package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.PacienteDAO;
import cat.copernic.proyecto2.DAO.JPA.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Acción de Agente para usuarios con rol ADMINISTRADOR.
 * Proporciona un resumen del estado actual de la base de datos.
 */
@Component
public class AccionAdmin implements TelegramAccion {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PacienteDAO pacienteDAO;

    @Override
    public boolean soporte(String mensaje) {
        // Define el comando que activa esta acción
        return mensaje.equals("/admin_status");
    }

    @Override
    public String ejecutar(Update update, Usuario usuario) {
        // Lógica de consulta a los DAOs
        long activos = usuarioDAO.findAll().stream()
                .filter(u -> u.isActivo())
                .count(); 
        
        long pacientes = pacienteDAO.count(); 

        return "📊 *Estado del Sistema*:\n" +
               "- Usuarios Activos: " + activos + "\n" +
               "- Total Pacientes registrados: " + pacientes;
    }

    @Override
    public String rolPermitido() {
        // Este valor debe coincidir con el nombre del Rol en tu base de datos
        return "ADMIN";
    }

    @Override
    public String descripcion() {
        return "Obtener un resumen administrativo del sistema (Solo Admin)";
    }
}