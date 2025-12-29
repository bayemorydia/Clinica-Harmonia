package cat.copernic.proyecto2.DAO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.util.List;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {
    boolean existsByDni(String dni);
    boolean existsByNumeroSeguridadSocial(String seguridadSocial);
    boolean existsByEmail(String email);
    List<Paciente> findByActivoTrue();
    long countByActivoTrue();
    // Búsqueda por Nombre completo o DNI con paginación
    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(CONCAT(p.nombre, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Paciente> findByNombreOrDni(String query, Pageable pageable);
}
