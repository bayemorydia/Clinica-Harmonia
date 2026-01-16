package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Diagnostico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiagnosticoDAO extends JpaRepository<Diagnostico, Long> {

    boolean existsByCodigo(String codigo);
    long countByActivoTrue();
    boolean existsByNombre(String nombre);

    @Query("SELECT d FROM Diagnostico d WHERE " +
            "(:activo IS NULL OR d.activo = :activo) AND " +
            "(LOWER(d.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.codigo) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Diagnostico> findByFilters(@Param("keyword") String keyword,
            @Param("activo") Boolean activo,
            Pageable pageable);
}