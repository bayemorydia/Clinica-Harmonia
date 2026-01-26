package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.Administración.Empleado; // Añadir import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface NominaDAO extends JpaRepository<Nomina, Long> {

    // AGREGAR ESTE MÉTODO para que concuerde con AccionEmpleado
    List<Nomina> findByEmpleado(Empleado empleado);

    boolean existsByEmpleadoIdAndPeriodo(Long empleadoId, String periodo);

    Page<Nomina> findByEmpleadoId(Long empleadoId, Pageable pageable);

    List<Nomina> findByPeriodo(String periodo);

    List<Nomina> findByPeriodoContaining(String anyo);

   Nomina  findByEmpleadoIdAndPeriodo(Long id, String periodo);
}