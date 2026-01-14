package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.PersonalSanitario.EpisodioClinico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodioClinicoDAO extends JpaRepository<EpisodioClinico, Long> {
}
