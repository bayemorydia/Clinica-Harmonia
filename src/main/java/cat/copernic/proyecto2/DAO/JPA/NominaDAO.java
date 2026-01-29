package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.Administración.Empleado; // Añadir import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface NominaDAO extends JpaRepository<Nomina, Long> {

    // AGREGAR ESTE MÉTODO para que concuerde con AccionEmpleado
    List<Nomina> findByEmpleado(Empleado empleado);

    boolean existsByEmpleadoIdAndPeriodo(Long empleadoId, String periodo);

    @Query("SELECT n FROM Nomina n WHERE n.empleado.id = :empleadoId AND " +
            "(LOWER(n.periodo) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Nomina> findByEmpleadoIdAndPeriodoContaining(@Param("empleadoId") Long empleadoId,
            @Param("query") String query,
            Pageable pageable);

    Page<Nomina> findByEmpleadoId(Long empleadoId, Pageable pageable);

    List<Nomina> findByPeriodo(String periodo);

    List<Nomina> findByPeriodoContaining(String anyo);

    Nomina findByEmpleadoIdAndPeriodo(Long id, String periodo);

    @Query("SELECT n FROM Nomina n WHERE " +
           "LOWER(n.empleado.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(n.empleado.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(n.periodo) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Nomina> findByGlobalQuery(@Param("query") String query, Pageable pageable);
    long countByPeriodo(String periodo);
}