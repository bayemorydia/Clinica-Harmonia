package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.NominaDAO;
import cat.copernic.proyecto2.DAO.JPA.VacacionesDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class AccionEmpleado implements TelegramAccion {

    @Autowired
    private NominaDAO nominaDAO;

    @Autowired
    private VacacionesDAO vacacionesDAO;

    @Override
    public boolean soporte(String mensaje) {
        return mensaje.equals("/mi_perfil") || mensaje.equals("/nominas") || mensaje.equals("/vacaciones");
    }

    @Override
    public String ejecutar(Update update, Usuario usuario) {
        Empleado emp = usuario.getEmpleado();
        if (emp == null) {
            return "No tienes perfil de empleado.";
        }
        
        String cmd = update.getMessage().getText();

        if (cmd.equals("/mi_perfil")) {
            return "👤 Perfil: " + emp.getNombre() + " " + emp.getApellidos();
        }

        if (cmd.equals("/nominas")) {
            // Asegúrate de tener el método List<Nomina> findByEmpleado(Empleado empleado) en NominaDAO
            var nominas = nominaDAO.findByEmpleado(emp); 
            if (nominas.isEmpty()) {
                return "No hay nóminas cargadas.";
            }
            return "💰 Tienes " + nominas.size() + " nóminas disponibles en el portal.";
        }

        if (cmd.equals("/vacaciones")) {
            // Asegúrate de tener el método List<Vacaciones> findByEmpleado(Empleado empleado) en VacacionesDAO
            List<Vacaciones> vacs = vacacionesDAO.findByEmpleado(emp); 
            if (vacs.isEmpty()) {
                return "No tienes solicitudes de vacaciones registradas.";
            }
            
            StringBuilder sb = new StringBuilder("🏖️ Vacaciones:\n");
            vacs.forEach(v -> sb.append("- ")
                    .append(v.getFechaInicio())
                    .append(" a ")
                    .append(v.getFechaFin())
                    .append(" (")
                    .append(v.getEstado())
                    .append(")\n"));
            return sb.toString();
        }
        
        return "Comando no reconocido.";
    }

    @Override
    public String rolPermitido() {
        return "EMPLEADO";
    }

    @Override
    public String descripcion() {
        return "Gestión de empleado (Nóminas, Vacaciones)";
    }
}