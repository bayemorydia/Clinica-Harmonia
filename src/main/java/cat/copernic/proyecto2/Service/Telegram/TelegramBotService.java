package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.PacienteDAO;
import cat.copernic.proyecto2.DAO.JPA.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
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
    private final List<TelegramAccion> acciones;

    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botName,
            UsuarioDAO usuarioDAO,
            PacienteDAO pacienteDAO,
            List<TelegramAccion> acciones) {
        super(botToken);
        this.botName = botName;
        this.usuarioDAO = usuarioDAO;
        this.pacienteDAO = pacienteDAO;
        this.acciones = acciones;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Ignorar si no es un mensaje de texto
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // 1. Recuperar el usuario usando Optional (Refactorizado)
        Optional<Usuario> usuarioOpt = usuarioDAO.findByTelegramChatId(chatId);

        // 2. Si no está presente, iniciar proceso de vinculación
        if (usuarioOpt.isEmpty()) {
            procesarVinculacionPaciente(chatId, text.trim().toUpperCase());
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // 3. Verificar estrictamente el rol PACIENTE
        boolean esPaciente = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("PACIENTE"));

        if (!esPaciente) {
            enviarMensaje(chatId, "🚫 *Acceso Restringido*: Este bot es de uso exclusivo para pacientes.");
            return;
        }

        // 4. Buscar y ejecutar la acción de paciente
        boolean accionEjecutada = false;
        for (TelegramAccion accion : acciones) {
            if (accion.soporte(text)) {
                // Solo ejecutamos si la acción es para PACIENTE (según tu requisito anterior)
                if ("PACIENTE".equals(accion.rolPermitido())) {
                    enviarMensaje(chatId, accion.ejecutar(update, usuario));
                    accionEjecutada = true;
                    break;
                }
            }
        }

        if (!accionEjecutada) {
            enviarMensaje(chatId, "👋 Hola *" + usuario.getUsername() + "*.\n" +
                    "Escribe *perfil*, *citas*, *facturas* o *tratamiento* para ayudarte.");
        }
    }

    private void procesarVinculacionPaciente(long chatId, String dni) {
        // Solo buscamos en la tabla de pacientes
        Optional<Paciente> pac = pacienteDAO.findByDni(dni);

        if (pac.isPresent()) {
            Usuario u = pac.get().getUsuario();
            if (u != null) {
                u.setTelegramChatId(chatId);
                usuarioDAO.save(u);
                enviarMensaje(chatId, "✅ *¡Identidad Confirmada!*\nBienvenido/a " + pac.get().getNombre() +
                        ". Ya puedes consultar tu información médica.");
            }
        } else {
            // Si el DNI no es válido o no es paciente, enviamos mensaje de
            // bienvenida/instrucciones
            enviarMensaje(chatId, "👋 *Bienvenido a Clínica Harmonía*\n\n" +
                    "Para acceder a tus datos, por favor introduce tu *DNI o NIE*.");
        }
    }

    public void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        message.setParseMode("Markdown"); // Cambiado a Markdown para coincidir con AccionPaciente
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}