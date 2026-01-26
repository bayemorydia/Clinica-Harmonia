package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
        boolean existsByUsername(String username);

        Page<Usuario> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

        Optional<Usuario> findByTelegramChatId(Long telegramChatId);

        @Query("SELECT u FROM Usuario u WHERE " +
                        "(:activo IS NULL OR u.activo = :activo) AND " +
                        "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Usuario> findByFilters(@Param("query") String query,
                        @Param("activo") Boolean activo,
                        Pageable pageable);

        Page<Usuario> findByUsername(String username, Pageable pageable);
}
