package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HistorialClinicoDAO extends JpaRepository<HistorialClinico, Long> {
    Optional<HistorialClinico> findByPacienteId(Long pacienteId);
}
