package cat.copernic.proyecto2.Dao;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


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


}

