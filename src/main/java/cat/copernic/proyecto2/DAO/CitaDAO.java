package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CitaDAO extends JpaRepository<Cita, Long> {
    Optional<Cita> findByPacienteAndFechaHora(Paciente paciente, LocalDateTime fechaHora);
    
    // Ahora buscamos por el nombre del médico escrito
    Optional<Cita> findByNombreMedicoAndFechaHora(String nombreMedico, LocalDateTime fechaHora);
}
