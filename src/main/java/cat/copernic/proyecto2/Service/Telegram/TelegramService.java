package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramService {
    boolean soporte(String mensaje);
    String ejecutar(Update update, Usuario usuario);
    String descripcion();
    String rolPermitido();
}