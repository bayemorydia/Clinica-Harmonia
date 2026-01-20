package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramAccion {
    boolean soporte(String mensaje); // Verifica si el texto coincide (ej: "/mis_citas")
    String ejecutar(Update update, Usuario usuario); // Procesa la lógica
    String descripcion(); // Para mostrar en el menú de ayuda
    String rolPermitido(); // "PACIENTE", "MEDICO", "ADMIN", "RECEPCION" o "TODOS"
}