package cat.copernic.proyecto2.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);
    boolean existsByNumeroSeguridadSocial(String seguridadSocial);
    boolean existsByEmail(String email);
}
