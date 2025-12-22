package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String username);
}
