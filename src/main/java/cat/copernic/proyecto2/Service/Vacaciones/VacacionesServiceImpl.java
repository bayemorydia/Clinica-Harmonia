package cat.copernic.proyecto2.Service.Vacaciones;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.DAO.VacacionesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VacacionesServiceImpl implements VacacionesService {

    @Autowired
    private VacacionesDAO vacacionesDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Vacaciones> listarVacacionesPorEmpleado(Long empleadoId) {
        return vacacionesDAO.findByEmpleadoId(empleadoId);
    }

    @Override
    @Transactional
    public void guardarVacacion(Vacaciones vacacion) {
        vacacionesDAO.save(vacacion);
    }

    @Override
    @Transactional
    public void eliminarVacacion(Long id) {
        vacacionesDAO.deleteById(id);
    }

    @Override
    public long calcularDiasDisponibles(Empleado empleado) {
        LocalDate hoy = LocalDate.now();
        // Si no hay fecha de alta (por seguridad), tomamos el 1 de enero del año actual
        LocalDate alta = (empleado.getFecha_alta() != null) ? empleado.getFecha_alta()
                : LocalDate.of(hoy.getYear(), 1, 1);

        int diasMaxAnuales = 22;

        // Si el alta fue en años anteriores, tiene los 22 días
        if (alta.getYear() < hoy.getYear()) {
            return diasMaxAnuales;
        }

        // Si entró este año, calculamos la parte proporcional hasta fin de año
        LocalDate finDeAnio = LocalDate.of(hoy.getYear(), 12, 31);
        long diasActivo = ChronoUnit.DAYS.between(alta, finDeAnio) + 1;

        return Math.round((diasActivo / 365.0) * diasMaxAnuales);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamiento(Long empleadoId, LocalDate inicio, LocalDate fin) {
        List<Vacaciones> existentes = vacacionesDAO.findByEmpleadoId(empleadoId);

        return existentes.stream()
                // Ignoramos las solicitudes rechazadas para que pueda volver a pedir esos días
                .filter(v -> v.getEstado() != EstadoVacaciones.RECHAZADA)
                .anyMatch(v -> (inicio.isBefore(v.getFechaFin().plusDays(1))
                        && fin.isAfter(v.getFechaInicio().minusDays(1))));
    }
}
