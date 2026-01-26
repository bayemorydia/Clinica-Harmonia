package cat.copernic.proyecto2.Service.Vacaciones;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import java.time.LocalDate;
import java.util.List;

public interface VacacionesService {
    List<Vacaciones> listarVacacionesPorEmpleado(Long empleadoId);
    void guardarVacacion(Vacaciones vacacion);
    void eliminarVacacion(Long id);
    long calcularDiasDisponibles(Empleado empleado);
    boolean existeSolapamiento(Long empleadoId, LocalDate inicio, LocalDate fin);
    
    // Nuevo método para orquestar la lógica que estaba en el controller
    void procesarSolicitud(String username, LocalDate inicio, LocalDate fin) throws Exception;
}