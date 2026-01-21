package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.*;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Validaciones.ValidacionesService;
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
    private final ValidacionesService validacionesService; // Inyectamos tu servicio

    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botName,
            UsuarioDAO usuarioDAO, PacienteDAO pacienteDAO, EmpleadoDAO empleadoDAO,
            CitaDAO citaDAO, FacturaDAO facturaDAO, NominaDAO nominaDAO,
            VacacionesDAO vacacionesDAO, ValidacionesService validacionesService) {
        super(botToken);
        this.botName = botName;
        this.usuarioDAO = usuarioDAO;
        this.pacienteDAO = pacienteDAO;
        this.empleadoDAO = empleadoDAO;
        this.citaDAO = citaDAO;
        this.facturaDAO = facturaDAO;
        this.nominaDAO = nominaDAO;
        this.vacacionesDAO = vacacionesDAO;
        this.validacionesService = validacionesService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        // 1. Buscamos si el dispositivo (chatId) ya está vinculado a un usuario
        Usuario usuario = usuarioDAO.findByTelegramChatId(chatId);

        // --- FLUJO PARA USUARIOS NO VINCULADOS ---
        if (usuario == null) {
            String dniCandidato = messageText.toUpperCase();

            try {
                // Intentamos validar si lo que ha escrito tiene formato de DNI/NIE
                validacionesService.validarDocumentoIdentidad(dniCandidato);

                // Si la validación no lanza excepción, procedemos a buscarlo en la DB
                procesarVinculacionDirecta(chatId, dniCandidato);

            } catch (RuntimeException e) {
                // Si falla la validación (no es un DNI o la letra está mal)
                enviarMensaje(chatId, "👋 <b>¡Hola! Bienvenido a la Clínica Harmonía.</b>\n\n" +
                        "No te reconozco. Por favor, <b>escribe tu DNI o NIE</b> para acceder a tu panel personal.");
            }
            return;
        }

        // --- FLUJO PARA USUARIOS YA VINCULADOS ---
        String respuesta = procesarComandosAgente(messageText.toLowerCase(), usuario);
        enviarMensaje(chatId, respuesta);
    }

    private void procesarVinculacionDirecta(long chatId, String dni) {
        // 1. Buscamos en Pacientes
        Optional<Paciente> pac = Optional.ofNullable(pacienteDAO.findByDni(dni));
        if (pac.isPresent()) {
            vincular(chatId, pac.get().getUsuario(), "Paciente: " + pac.get().getNombre());
            return;
        }

        // 2. Si no es paciente, buscamos en Empleados
        Optional<Empleado> emp = empleadoDAO.findByDni(dni);
        if (emp.isPresent()) {
            vincular(chatId, emp.get().getUsuario(), "Empleado: " + emp.get().getNombre());
            return;
        }

        // 3. Si el DNI es válido pero no existe en nuestra DB
        enviarMensaje(chatId, "❌ El DNI/NIE <b>" + dni
                + "</b> no consta en nuestra base de datos. Por favor, contacta con administración.");
    }

    private void vincular(long chatId, Usuario u, String info) {
        if (u != null) {
            u.setTelegramChatId(chatId);
            usuarioDAO.save(u);
            enviarMensaje(chatId, "✅ <b>¡Identidad confirmada!</b>\nVinculado como " + info +
                    ".\n\nEscribe <b>'hola'</b> para ver qué puedo hacer por ti.");
        } else {
            enviarMensaje(chatId,
                    "⚠️ Tu perfil existe, pero no tienes un usuario de acceso creado. Contacta con soporte.");
        }
    }

    private String procesarComandosAgente(String cmd, Usuario usuario) {
        StringBuilder ayuda = new StringBuilder("<b>Comandos disponibles:</b>\n\n");

        // Lógica de Paciente
        if (usuario.getPaciente() != null) {
            Paciente p = usuario.getPaciente();
            if (cmd.contains("mi perfil"))
                return "👤 <b>Perfil</b>:\n- " + p.getNombre() + " " + p.getApellidos() + "\n- DNI: " + p.getDni();
            if (cmd.contains("mis citas")) {
                var citas = citaDAO.findByPaciente(p);
                if (citas.isEmpty())
                    return "No tienes citas programadas.";
                StringBuilder sb = new StringBuilder("📅 <b>Tus Citas</b>:\n");
                citas.forEach(c -> sb.append("- ").append(c.getFechaHora()).append(" (").append(c.getEspecialidad())
                        .append(")\n"));
                return sb.toString();
            }
            ayuda.append("• mi perfil\n• mis citas\n• facturas\n");
        }

        // Lógica de Empleado
        if (usuario.getEmpleado() != null) {
            Empleado e = usuario.getEmpleado();
            if (cmd.contains("nominas"))
                return "💰 Tienes " + nominaDAO.findByEmpleado(e).size() + " nóminas.";
            ayuda.append("• mi perfil\n• nominas\n• vacaciones\n");
        }

        return "👋 Hola <b>" + usuario.getUsername() + "</b>\n\n" + ayuda.toString();
    }

    public void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        message.setParseMode("HTML"); // Importante para que las negritas funcionen sin errores
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}