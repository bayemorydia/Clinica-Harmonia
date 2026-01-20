package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface RolDAO extends JpaRepository<Rol, Long> {

        Optional<Rol> findByNombre(String nombre);

        @Query("SELECT r FROM Rol r WHERE " +
                        "(:activo IS NULL OR r.activo = :activo) AND " +
                        "(LOWER(r.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Rol> findByFilters(@Param("query") String query,
                        @Param("activo") Boolean activo,
                        Pageable pageable);

    @Query("SELECT r FROM Rol r WHERE r.nombre IN ('RRHH', 'RECEPCIONISTA', 'MEDICO')")
    List<Rol> findRolesEspecificos();

}