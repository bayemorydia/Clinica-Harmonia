package cat.copernic.proyecto2.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;

public interface PacienteService {
    public List<Paciente> getAllPacientes();
    public void savePaciente(Paciente paciente, MultipartFile imagen);
    public Paciente findPacienteById(Long id);
    public void deletePaciente(Long id);
    public String getEdadPaciente(Paciente paciente);
}
