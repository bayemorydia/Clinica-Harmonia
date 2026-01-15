package cat.copernic.proyecto2.DAO.JPA;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {
    boolean existsByDni(String dni);
    boolean existsByNumeroSeguridadSocial(String seguridadSocial);
    boolean existsByEmail(String email);
    long countByActivoTrue();
    @Query("SELECT p FROM Paciente p WHERE " +
           "(:activo IS NULL OR p.activo = :activo) AND " +
           "(LOWER(CONCAT(p.nombre, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Paciente> findByFilters(@Param("query") String query, 
                                 @Param("activo") Boolean activo, 
                                 Pageable pageable);
}