package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.*;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class TelegramBotService extends TelegramLongPollingBot {

    private final String botName;
    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final EmpleadoDAO empleadoDAO;
    private final CitaDAO citaDAO;
    private final FacturaDAO facturaDAO;
    private final NominaDAO nominaDAO;
    private final VacacionesDAO vacacionesDAO;

    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botName,
            UsuarioDAO usuarioDAO, PacienteDAO pacienteDAO, EmpleadoDAO empleadoDAO,
            CitaDAO citaDAO, FacturaDAO facturaDAO, NominaDAO nominaDAO, VacacionesDAO vacacionesDAO) {
        super(botToken);
        this.botName = botName;
        this.usuarioDAO = usuarioDAO;
        this.pacienteDAO = pacienteDAO;
        this.empleadoDAO = empleadoDAO;
        this.citaDAO = citaDAO;
        this.facturaDAO = facturaDAO;
        this.nominaDAO = nominaDAO;
        this.vacacionesDAO = vacacionesDAO;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String messageText = update.getMessage().getText().toLowerCase().trim();
        long chatId = update.getMessage().getChatId();

        // 1. Lógica de Login
        if (messageText.startsWith("/login ")) {
            procesarLogin(chatId, messageText.substring(7).trim());
            return;
        }

        // 2. Identificar Usuario
        Usuario usuario = usuarioDAO.findByTelegramChatId(chatId);
        if (usuario == null) {
            enviarMensaje(chatId, "🚫 No estás identificado. Usa /login SU_DNI");
            return;
        }

        // 3. Procesar según Rol
        String respuesta = procesarComandosAgente(messageText, usuario);
        enviarMensaje(chatId, respuesta);
    }

    private String procesarComandosAgente(String cmd, Usuario usuario) {
        StringBuilder ayuda = new StringBuilder("👋 Hola *" + usuario.getUsername() + "*, estos son los comandos que puedes usar:\n\n");
        boolean comandoReconocido = false;

        // --- SECCIÓN PACIENTE ---
        if (usuario.getPaciente() != null) {
            Paciente p = usuario.getPaciente();
            ayuda.append("• *mi perfil*: Ver tus datos personales.\n");
            ayuda.append("• *mis citas*: Ver tus próximas visitas.\n");
            ayuda.append("• *facturas*: Ver tus facturas médicas.\n");

            if (cmd.contains("mi perfil") || cmd.equals("/mi_perfil")) {
                return "👤 *Tu Perfil de Paciente*:\n- Nombre: " + p.getNombre() + " " + p.getApellidos() + "\n- DNI: " + p.getDni() + "\n- Teléfono: " + p.getTelefono();
            }
            if (cmd.contains("mis citas") || cmd.equals("/mis_citas")) {
                var citas = citaDAO.findByPaciente(p);
                if (citas.isEmpty()) return "No tienes citas programadas.";
                StringBuilder sb = new StringBuilder("📅 *Tus Citas*:\n");
                citas.forEach(c -> sb.append("- ").append(c.getFechaHora()).append(" (").append(c.getEspecialidad()).append(")\n"));
                return sb.toString();
            }
            if (cmd.contains("facturas") || cmd.equals("/facturas")) {
                var facturas = facturaDAO.findByPaciente(p);
                if (facturas.isEmpty()) return "No tienes facturas registradas.";
                StringBuilder sb = new StringBuilder("🧾 *Tus Facturas*:\n");
                facturas.forEach(f -> sb.append("- Factura #").append(f.getId()).append(" de fecha ").append(f.getFechaEmision()).append(" | Total: ").append(f.getTotal()).append("€\n"));
                return sb.toString();
            }
            if (!cmd.contains("hola")) comandoReconocido = true;
        }

        // --- SECCIÓN EMPLEADO (Médicos/RRHH) ---
        if (usuario.getEmpleado() != null) {
            Empleado e = usuario.getEmpleado();
            ayuda.append("• *mi perfil*: Ver tus datos profesionales.\n");
            ayuda.append("• *nominas*: Consultar tus nóminas.\n");
            ayuda.append("• *vacaciones*: Ver estado de tus vacaciones.\n");

            if (cmd.contains("mi perfil") || cmd.equals("/mi_perfil")) {
                return "👤 *Perfil Profesional*:\n- Nombre: " + e.getNombre() + " " + e.getApellidos();
            }
            if (cmd.contains("nominas") || cmd.equals("/nominas")) {
                var nominas = nominaDAO.findByEmpleado(e);
                return nominas.isEmpty() ? "No hay nóminas disponibles." : "💰 Tienes " + nominas.size() + " nóminas en el sistema.";
            }
            if (cmd.contains("vacaciones") || cmd.equals("/vacaciones")) {
                var vacs = vacacionesDAO.findByEmpleado(e);
                if (vacs.isEmpty()) return "No tienes solicitudes de vacaciones.";
                StringBuilder sb = new StringBuilder("🏖️ *Tus Vacaciones*:\n");
                vacs.forEach(v -> sb.append("- ").append(v.getFechaInicio()).append(" al ").append(v.getFechaFin()).append(" (").append(v.getEstado()).append(")\n"));
                return sb.toString();
            }
            if (!cmd.contains("hola")) comandoReconocido = true;
        }

        // --- SECCIÓN ADMIN ---
        boolean isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("ADMIN"));
        if (isAdmin) {
            ayuda.append("• *estado*: Resumen general del sistema.\n");
            if (cmd.contains("estado") || cmd.equals("/admin_status")) {
                long activos = usuarioDAO.findAll().stream().filter(Usuario::isActivo).count();
                long totalPacientes = pacienteDAO.count();
                return "📊 *Estado del Sistema*:\n- Usuarios Activos: " + activos + "\n- Total Pacientes: " + totalPacientes;
            }
            if (!cmd.contains("hola")) comandoReconocido = true;
        }

        // Si el usuario escribió "hola" o un comando que no procesamos arriba
        return ayuda.toString();
    }

    private void procesarLogin(long chatId, String dni) {
        Optional<Paciente> pac = Optional.ofNullable(pacienteDAO.findByDni(dni));
        if (pac.isPresent()) {
            vincular(chatId, pac.get().getUsuario(), "Paciente: " + pac.get().getNombre());
            return;
        }
        Optional<Empleado> emp = empleadoDAO.findByDni(dni);
        if (emp.isPresent()) {
            vincular(chatId, emp.get().getUsuario(), "Empleado: " + emp.get().getNombre());
            return;
        }
        enviarMensaje(chatId, "❌ DNI no encontrado.");
    }

    private void vincular(long chatId, Usuario u, String info) {
        if (u != null) {
            u.setTelegramChatId(chatId);
            usuarioDAO.save(u);
            enviarMensaje(chatId, "✅ Vinculado como " + info);
        }
    }

    public void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}