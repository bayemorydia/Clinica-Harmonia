package cat.copernic.proyecto2.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.DAO.PacienteDAO;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import jakarta.transaction.Transactional;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteDAO pacienteDAO;

    @Autowired
    private ImagenService imagenService;

    @Override
    public List<Paciente> getAllPacientes() {
        List<Paciente> pacientes = pacienteDAO.findAll();
        return pacientes;
    }

    @Override
    @Transactional
    public void savePaciente(Paciente paciente, MultipartFile file) {
        String nombreImagenNueva = imagenService.guardarImagen(file, "Paciente");
        if (paciente.getId() == null || paciente.getId() == 0) {
            paciente.setFechaAlta(LocalDate.now());
            validarDuplicados(paciente);            
            if (nombreImagenNueva != null) {
                paciente.setRutaFoto(nombreImagenNueva);
            } else {
                paciente.setRutaFoto("default-user.png");
            }
        } else {
            Paciente existente = pacienteDAO.findById(paciente.getId()).orElse(null);
            if (existente != null) {
                paciente.setFechaAlta(existente.getFechaAlta());                
                if (nombreImagenNueva != null) {
                    imagenService.eliminarImagen(existente.getRutaFoto(), "Paciente");
                    paciente.setRutaFoto(nombreImagenNueva);
                } else {
                    paciente.setRutaFoto(existente.getRutaFoto());
                }
            }
        }
        pacienteDAO.save(paciente);
    }

    private void validarDuplicados(Paciente paciente) {
        if (pacienteDAO.existsByDni(paciente.getDni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }
        if (pacienteDAO.existsByTelefono(paciente.getTelefono())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }
        if (pacienteDAO.existsByNumeroSeguridadSocial(paciente.getNumeroSeguridadSocial())) {
            throw new RuntimeException("El número de Seguridad Social ya está registrado");
        }
        if (pacienteDAO.existsByEmail(paciente.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
    }

    @Override
    public Paciente findPacienteById(Long id) {
        return pacienteDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deletePaciente(Long id) {
        Paciente p = pacienteDAO.findById(id).orElse(null);
        if (p != null) {
            imagenService.eliminarImagen(p.getRutaFoto(), "Paciente");
            pacienteDAO.delete(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        Period periodo = Period.between(paciente.getFechaNacimiento(), LocalDate.now());
        if (periodo.getYears() >= 1) {
            return periodo.getYears() + (periodo.getYears() == 1 ? " año" : " años");
        }
        if (periodo.getMonths() >= 1) {
            return periodo.getMonths() + (periodo.getMonths() == 1 ? " mes" : " meses");
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(paciente.getFechaNacimiento(), LocalDate.now());
        if (dias <= 0) {
            return "Recién nacido";
        }
        return dias + (dias == 1 ? " día" : " días");
    }
}
