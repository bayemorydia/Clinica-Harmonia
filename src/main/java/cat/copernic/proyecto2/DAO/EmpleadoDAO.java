package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import java.util.Optional;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
public interface EmpleadoDAO extends JpaRepository<Empleado, Long> {
    @Query("SELECT e FROM  Empleado e WHERE e.rol  = : valor ")
    List<Empleado> obtenerPorRol(@Param("valor") Rol rol);

    @Query("SELECT e FROM Empleado e WHERE LOWER (e.departamento) LIKE LOWER('%valor%') ")
    List<Empleado> obtenerPorDepartamento(@Param("valor") String departamento);

    @Query("SELECT e FROM Empleado e WHERE e.fecha_baja IS NULL ")
    List<Empleado> obtenerActivos();

    @Query("SELECT e FROM Empleado e WHERE LOWER(e.nombre) LIKE LOWER('%valor%') OR LOWER(e.apellidos) LIKE LOWER('%valor%')")
    List<Empleado> obtenerActivosPorNombreOApellido(@Param("valor") String nombre);

    @Query("SELECT e FROM Empleado e WHERE e.email = :valor")
    Optional<Empleado> obtenerEmail(@Param("valor") String email);

    @Query("SELECT e FROM Empleado e WHERE e.dni = :valor")
    Optional<Empleado> obtenerPorDni(@Param("valor") String dni);

    @Query("SELECT e FROM Empleado e WHERE e.telefono = :valor")
    Optional<Empleado> obtenerTelefono(@Param("valor") String telefono);

    @Query("SELECT e FROM Empleado e WHERE e.dni = :valor")
    Optional<Empleado> obtenerDNI(@Param("valor") String DNI);

    // En EmpleadoDAO.java
    Optional<Empleado> findByUsuarioUsername(String username);

    @Query("SELECT n FROM Nomina n " +
            "WHERE n.empleado.id = :empleadoId " +
            "AND (:search IS NULL OR LOWER(n.periodo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY n.periodo DESC")
    Page<Nomina> findByEmpleado(
            @Param("empleadoId") Long empleadoId,
            @Param("search") String search,
            Pageable pageable
    );


    @Query("select count(e) from Empleado e")
    int BuscarEmpleadosTotal();

}
