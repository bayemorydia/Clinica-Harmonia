package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmpleadoDAO extends JpaRepository<Empleado, Long> {

        @Query("SELECT e FROM Empleado e WHERE " +
                        "(:activa IS NULL OR e.activa = :activa) AND " +
                        "(LOWER(CONCAT(e.nombre, ' ', e.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(e.dni) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Empleado> findByFilters(@Param("query") String query,
                        @Param("activa") Boolean activa,
                        Pageable pageable);

        @Query("SELECT n FROM Nomina n WHERE n.empleado.id = :empleadoId AND " +
                        "(:search IS NULL OR LOWER(CAST(n.id AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Nomina> findByEmpleado(@Param("empleadoId") Long empleadoId,
                        @Param("search") String search,
                        Pageable pageable);

        long countByActivaTrue();

        // Métodos de validación y búsqueda existentes
        @Query("SELECT e FROM Empleado e WHERE e.dni = :dni")
        Optional<Empleado> findByDni(@Param("dni") String dni);

        @Query("SELECT e FROM Empleado e WHERE e.email = :email")
        Optional<Empleado> findByEmail(@Param("email") String email);

        @Query("SELECT e FROM Empleado e WHERE e.telefono = :telefono")
        Optional<Empleado> findByTelefono(@Param("telefono") String telefono);

        Optional<Empleado> findByUsuarioUsername(String username);


}