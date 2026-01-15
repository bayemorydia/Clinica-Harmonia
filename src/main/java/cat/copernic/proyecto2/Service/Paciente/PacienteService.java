package cat.copernic.proyecto2.Service.Paciente;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteService {
    List<Paciente> getAllPacientes();
    void savePaciente(Paciente paciente, MultipartFile imagen);
    Paciente findPacienteById(Long id);
    void deletePaciente(Long id);
    String getEdadPaciente(Paciente paciente);
    long countPacientesActivos();
    // Cambiado de boolean a Boolean para permitir nulos (todos)
    Page<Paciente> getPacientesPaginados(String query, Boolean activo, int page, int size);
}