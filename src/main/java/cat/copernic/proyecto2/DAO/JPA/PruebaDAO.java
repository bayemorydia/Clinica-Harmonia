package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PruebaDAO extends JpaRepository<PruebaMedica, Long> {

        @Query("SELECT p FROM PruebaMedica p WHERE " +
                        "(:activo IS NULL OR p.activo = :activo) AND " +
                        "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<PruebaMedica> findByFilters(@Param("query") String query,
                        @Param("activo") Boolean activo,
                        Pageable pageable);

        Optional<PruebaMedica> findByCodigo(String codigo);

        Optional<PruebaMedica> findByNombre(String nombre);

        long countByActivoTrue();
}