package cat.copernic.proyecto2.Service.Vacaciones;

import cat.copernic.proyecto2.DAO.JPA.VacacionesDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;

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
    @Transactional(readOnly = true)
    public long calcularDiasDisponibles(Empleado empleado) {
        LocalDate hoy = LocalDate.now();
        LocalDate alta = (empleado.getFecha_alta() != null) ? empleado.getFecha_alta()
                : LocalDate.of(hoy.getYear(), 1, 1);

        int diasMaxAnuales = 22;
        long diasDerecho;

        if (alta.getYear() < hoy.getYear()) {
            diasDerecho = diasMaxAnuales;
        } else {
            LocalDate finDeAnio = LocalDate.of(hoy.getYear(), 12, 31);
            long diasActivo = ChronoUnit.DAYS.between(alta, finDeAnio) + 1;
            diasDerecho = Math.round((diasActivo / 365.0) * diasMaxAnuales);
        }

        // NUEVO: Restar los días de vacaciones ya ACEPTADAS este año
        List<Vacaciones> misVacaciones = vacacionesDAO.findByEmpleadoId(empleado.getId());
        long diasConsumidos = misVacaciones.stream()
                .filter(v -> v.getEstado() == EstadoVacaciones.ACEPTADA) // Solo las aprobadas cuentan
                .filter(v -> v.getFechaInicio().getYear() == hoy.getYear())
                .mapToLong(Vacaciones::getTotalDias)
                .sum();

        return diasDerecho - diasConsumidos;
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
