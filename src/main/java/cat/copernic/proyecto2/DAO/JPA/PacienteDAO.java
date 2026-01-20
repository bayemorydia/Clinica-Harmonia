package cat.copernic.proyecto2.DAO.JPA;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {

       // --- Validaciones booleanas ---
       boolean existsByDni(String dni);

       boolean existsByNumeroSeguridadSocial(String seguridadSocial);

       boolean existsByEmail(String email);

       @Query("SELECT p FROM Paciente p WHERE " +
                     "(:activo IS NULL OR p.activo = :activo) AND " +
                     "(LOWER(CONCAT(p.nombre, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                     "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%')))")
       Page<Paciente> findByFilters(@Param("query") String query,
                     @Param("activo") Boolean activo,
                     Pageable pageable);

       // --- NUEVO: ESTE ES EL QUE TE FALTABA PARA QUE FUNCIONE EL SERVICE ---
       // Devuelve el objeto Paciente entero buscando por DNI
       Paciente findByDni(String dni);

       // --- Consultas de Activos ---
       List<Paciente> findByActivoTrue();

       long countByActivoTrue();

       // --- Buscador ---
       // He añadido @Param("query") que es obligatorio para que funcione :query
       @Query("SELECT p FROM Paciente p WHERE " +
                     "LOWER(CONCAT(p.nombre, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                     "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%'))")
       Page<Paciente> findByNombreOrDni(@Param("query") String query, Pageable pageable);

       Optional<Paciente> findByUsuarioUsername(String username);
}