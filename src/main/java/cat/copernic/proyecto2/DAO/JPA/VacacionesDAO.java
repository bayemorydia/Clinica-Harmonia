package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VacacionesDAO extends JpaRepository<Vacaciones, Long> {
    List<Vacaciones> findByEmpleadoId(Long empleadoId);
}
