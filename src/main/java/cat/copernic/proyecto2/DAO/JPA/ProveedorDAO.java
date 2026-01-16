package cat.copernic.proyecto2.DAO.JPA;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProveedorDAO extends JpaRepository<Proveedor, Long> {

    @Query("SELECT p FROM Proveedor p WHERE " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.cif) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Proveedor> findByFilters(@Param("query") String query,
            @Param("activo") Boolean activo,
            Pageable pageable);

    Optional<Proveedor> findByCif(String cif);

    Optional<Proveedor> findByEmail(String email);

    Optional<Proveedor> findByTelefono(String telefono);
}