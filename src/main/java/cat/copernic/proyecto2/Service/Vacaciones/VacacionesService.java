package cat.copernic.proyecto2.Service.Vacaciones;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;

import java.time.LocalDate;
import java.util.List;

public interface VacacionesService {
    public List<Vacaciones> listarVacacionesPorEmpleado(Long empleadoId);
    public void guardarVacacion(Vacaciones vacacion);
    public void eliminarVacacion(Long id);
    public long calcularDiasDisponibles(Empleado empleado);
    boolean existeSolapamiento(Long empleadoId, LocalDate inicio, LocalDate fin);
}
