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
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class TelegramBotService extends TelegramLongPollingBot {

    private final String botName;
    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final EmpleadoDAO empleadoDAO;
    private final ValidacionesService validacionesService;
    private final List<TelegramAccion> acciones; // Lista de todas las acciones (AccionPaciente, etc.)

    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botName,
            UsuarioDAO usuarioDAO, PacienteDAO pacienteDAO, EmpleadoDAO empleadoDAO,
            ValidacionesService validacionesService,
            List<TelegramAccion> acciones) {
        super(botToken);
        this.botName = botName;
        this.usuarioDAO = usuarioDAO;
        this.pacienteDAO = pacienteDAO;
        this.empleadoDAO = empleadoDAO;
        this.validacionesService = validacionesService;
        this.acciones = acciones;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        Usuario usuario = usuarioDAO.findByTelegramChatId(chatId);

        if (usuario == null) {
            String dniCandidato = messageText.trim().toUpperCase();
            try {
                validacionesService.validarDocumentoIdentidad(dniCandidato);
                procesarVinculacionDirecta(chatId, dniCandidato);
            } catch (RuntimeException e) {
                enviarMensaje(chatId, "👋 <b>¡Hola! Bienvenido a la Clínica Harmonía.</b>\n\n" +
                        "No te reconozco. Por favor, <b>escribe tu DNI o NIE</b> para acceder.");
            }
            return;
        }

        // BUSCAR ACCIÓN COMPATIBLE
        String respuesta = null;
        for (TelegramAccion accion : acciones) {
            if (accion.soporte(messageText)) {
                // Verificar si el usuario tiene el rol necesario
                boolean tieneRol = usuario.getRoles().stream()
                        .anyMatch(r -> r.getNombre().equalsIgnoreCase(accion.rolPermitido()));
                
                if (tieneRol || accion.rolPermitido().equals("TODOS")) {
                    respuesta = accion.ejecutar(update, usuario);
                    break;
                }
            }
        }

        if (respuesta == null) {
            respuesta = generarMenuAyuda(usuario);
        }

        enviarMensaje(chatId, respuesta);
    }

    private String generarMenuAyuda(Usuario usuario) {
        StringBuilder sb = new StringBuilder("👋 Hola <b>" + usuario.getUsername() + "</b>\n\n");
        sb.append("<b>Comandos disponibles:</b>\n");
        for (TelegramAccion acc : acciones) {
            boolean tieneRol = usuario.getRoles().stream()
                    .anyMatch(r -> r.getNombre().equalsIgnoreCase(acc.rolPermitido()));
            if (tieneRol) {
                sb.append("• ").append(acc.descripcion()).append("\n");
            }
        }
        return sb.toString();
    }

    private void procesarVinculacionDirecta(long chatId, String dni) {
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
        enviarMensaje(chatId, "❌ El DNI <b>" + dni + "</b> no consta en nuestra base de datos.");
    }

    private void vincular(long chatId, Usuario u, String info) {
        if (u != null) {
            u.setTelegramChatId(chatId);
            usuarioDAO.save(u);
            enviarMensaje(chatId, "✅ <b>¡Identidad confirmada!</b>\nVinculado como " + info + ".");
        }
    }

    public void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        message.setParseMode("HTML");
        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }
}