package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicamentoDAO extends JpaRepository<Medicamento, Long> {
    Medicamento findByNombre(String nombre);

    Medicamento findByCodigo(String codigo);

    List<Medicamento> findByNombreContainingIgnoreCase(String nombre);

    long countByActivoTrue();

    @Query("SELECT m FROM Medicamento m WHERE " +
            "(:activo IS NULL OR m.activo = :activo) AND " +
            "(LOWER(m.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.codigo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.tipo) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Medicamento> findByFilters(@Param("query") String query,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
