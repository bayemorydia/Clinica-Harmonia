package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.Administración.Empleado; // Añadir import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VacacionesDAO extends JpaRepository<Vacaciones, Long> {
    // Método actual que ya tienes
    List<Vacaciones> findByEmpleadoId(Long empleadoId);

    // AGREGAR ESTE MÉTODO para que concuerde con AccionEmpleado
    List<Vacaciones> findByEmpleado(Empleado empleado);

    @Query("select count(e) from Vacaciones e where e.estado= 'PENDIENTE'")
    long contarPendiente();
}