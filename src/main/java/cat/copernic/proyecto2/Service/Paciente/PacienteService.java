package cat.copernic.proyecto2.Service.Paciente;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteService {
    List<Paciente> getAllPacientes();
    void savePaciente(Paciente paciente, MultipartFile imagen);
    Paciente findPacienteById(Long id);
    void deletePaciente(Long id);
    String getEdadPaciente(Paciente paciente);
}
