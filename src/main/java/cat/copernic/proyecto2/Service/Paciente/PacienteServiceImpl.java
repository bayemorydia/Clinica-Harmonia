package cat.copernic.proyecto2.Service.Paciente;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.proyecto2.DAO.JPA.PacienteDAO;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Imagen.ImagenService;
import cat.copernic.proyecto2.Service.Validaciones.ValidacionesService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import jakarta.transaction.Transactional;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteDAO pacienteDAO;

    @Autowired
    private ImagenService imagenService;

    @Autowired
    private ValidacionesService validacionesService;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Paciente> getAllPacientes() {
        return pacienteDAO.findAll();
    }

    @Override
    @Transactional
    public void savePaciente(Paciente paciente, MultipartFile file) {
        String nombreImagenNueva = imagenService.guardarImagen(file, "Paciente");

        if (paciente.getDni() != null) {
            paciente.setDni(paciente.getDni().toUpperCase().trim());
        }

        if (paciente.getId() == null || paciente.getId() == 0) {
            validacionesService.validarDocumentoIdentidad(paciente.getDni());
            validarDuplicados(paciente);
            paciente.setFechaAlta(LocalDate.now());
            paciente.setActivo(true); // Asegurar que nace activo

            Usuario nuevoUsuario = usuarioService.crearUsuarioPaciente(
                    paciente.getNombre(), paciente.getApellidos(), paciente.getDni());
            paciente.setUsuario(nuevoUsuario);

            paciente.setRutaFoto(nombreImagenNueva != null ? nombreImagenNueva : "default-user.png");
        } else {
            Paciente existente = pacienteDAO.findById(paciente.getId()).orElse(null);
            if (existente != null) {
                paciente.setFechaAlta(existente.getFechaAlta());
                paciente.setUsuario(existente.getUsuario());

                if (!existente.getDni().equals(paciente.getDni())) {
                    validacionesService.validarDocumentoIdentidad(paciente.getDni());
                    validarDuplicados(paciente);
                }

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
        if (pacienteDAO.existsByDni(paciente.getDni())) throw new RuntimeException("DNI duplicado");
        if (pacienteDAO.existsByNumeroSeguridadSocial(paciente.getNumeroSeguridadSocial())) throw new RuntimeException("SS duplicada");
        if (pacienteDAO.existsByEmail(paciente.getEmail())) throw new RuntimeException("Email duplicado");
    }

    @Override
    public Paciente findPacienteById(Long id) {
        return pacienteDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deletePaciente(Long id) {
        Paciente p = findPacienteById(id);
        if (p != null) {
            p.setActivo(false);
            if (p.getUsuario() != null) p.getUsuario().setActivo(false);
            pacienteDAO.save(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        if (paciente == null || paciente.getFechaNacimiento() == null) return "N/A";
        Period p = Period.between(paciente.getFechaNacimiento(), LocalDate.now());
        return p.getYears() + " años";
    }

    @Override
    public long countPacientesActivos() {
        return pacienteDAO.countByActivoTrue();
    }

    @Override
    public Page<Paciente> getPacientesPaginados(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending());
        return pacienteDAO.findByFilters(query, activo, pageable);
    }
}