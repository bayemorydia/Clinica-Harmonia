package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolDAO extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
    Page<Rol> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}