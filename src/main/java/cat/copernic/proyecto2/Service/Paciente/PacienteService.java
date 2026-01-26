package cat.copernic.proyecto2.Service.Paciente;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteService {

    List<Paciente> getAllPacientes();

    void savePaciente(Paciente paciente, MultipartFile imagen);

    // Nuevo método para mover la lógica de actualización de foto desde controller
    void actualizarFotoDetalle(Long id, MultipartFile file);

    Paciente findPacienteById(Long id);

    Paciente encontrarPorDni(String dni);

    void desactivarPaciente(Long id);

    String getEdadPaciente(Paciente paciente);

    long countPacientesActivos();

    Page<Paciente> getPacientesPaginados(String query, Boolean activo, int page, int size);

    Paciente buscarPorUsername(String username);

    boolean existeDni(String dni, Long idActual);

    void activarPaciente(Long id);

    // Añadir a la interfaz PacienteService
    long obtenerTotalPacientes();
}