package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VacacionesDAO extends JpaRepository<Vacaciones, Long> {
    List<Vacaciones> findByEmpleadoId(Long empleadoId);
    @Query("select count(e) from Vacaciones e where e.estado= 'PENDIENTE'")
    long contarPendiente();
}
