package cat.copernic.proyecto2.Service.Paciente;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteService {

    List<Paciente> getAllPacientes();

    void savePaciente(Paciente paciente, MultipartFile imagen);

    void actualizarFotoDetalle(Long id, MultipartFile file);

    Paciente findPacienteById(Long id);

    void desactivarPaciente(Long id);

    String getEdadPaciente(Paciente paciente);

    List<Paciente> listarPacientesActivos();

    long countPacientesActivos();

    Page<Paciente> getPacientesPaginados(String query, Boolean activo, int page, int size);

    Paciente buscarPorUsername(String username);

    void activarPaciente(Long id);

    long obtenerTotalPacientes();
}