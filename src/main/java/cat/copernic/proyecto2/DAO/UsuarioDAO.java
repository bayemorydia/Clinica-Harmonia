package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String username);
    Page<Usuario> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
