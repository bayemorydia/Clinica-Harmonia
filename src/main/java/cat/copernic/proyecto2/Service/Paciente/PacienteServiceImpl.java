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

    @Autowired(required = false) // Opcional por si fallaba la inyección
    private ImagenService imagenService;

    @Autowired(required = false)
    private ValidacionesService validacionesService;

    @Autowired(required = false)
    private UsuarioService usuarioService;

    @Override
    public List<Paciente> getAllPacientes() {
        return pacienteDAO.findAll();
    }

    @Override
    @Transactional
    public void savePaciente(Paciente paciente, MultipartFile file) {
        String nombreImagenNueva = null;

        if (imagenService != null && file != null && !file.isEmpty()) {
             nombreImagenNueva = imagenService.guardarImagen(file, "Paciente");
        }

        if (paciente.getDni() != null) {
            paciente.setDni(paciente.getDni().toUpperCase().trim());
        }

        if (paciente.getId() == null || paciente.getId() == 0) {
            // NUEVO
            if (validacionesService != null) validacionesService.validarDocumentoIdentidad(paciente.getDni());
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
                     if (validacionesService != null) validacionesService.validarDocumentoIdentidad(paciente.getDni());
                     validarDuplicados(paciente);
                }

                if (nombreImagenNueva != null) {
                    if (imagenService != null) imagenService.eliminarImagen(existente.getRutaFoto(), "Paciente");
                    paciente.setRutaFoto(nombreImagenNueva);
                } else {
                    paciente.setRutaFoto(existente.getRutaFoto());
                }
            }
        }
        pacienteDAO.save(paciente);
    }

    private void validarDuplicados(Paciente paciente) {
        if (paciente.getId() == null || paciente.getId() == 0) {
            if (pacienteDAO.existsByDni(paciente.getDni())) {
                throw new RuntimeException("El DNI/NIE ya está registrado en el sistema");
            }
            if (paciente.getNumeroSeguridadSocial() != null && !paciente.getNumeroSeguridadSocial().isEmpty() 
                    && pacienteDAO.existsByNumeroSeguridadSocial(paciente.getNumeroSeguridadSocial())) {
                throw new RuntimeException("El número de Seguridad Social ya está registrado");
            }
            if (paciente.getEmail() != null && !paciente.getEmail().isEmpty() 
                    && pacienteDAO.existsByEmail(paciente.getEmail())) {
                throw new RuntimeException("El correo electrónico ya está registrado");
            }
        }
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
            if (p.getUsuario() != null) {
                p.getUsuario().setActivo(false);
            }
            pacienteDAO.save(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        if (paciente == null || paciente.getFechaNacimiento() == null) return "Fecha no disponible";
        Period periodo = Period.between(paciente.getFechaNacimiento(), LocalDate.now());
        return periodo.getYears() + " años"; // Simplificado
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

    // --- EL ÚNICO MÉTODO EXTRA NECESARIO ---
    @Override
    public boolean existeDni(String dni, Long idActual) {
        Paciente encontrado = pacienteDAO.findByDni(dni);
        if (encontrado == null) return false;
        if (idActual != null && encontrado.getId().equals(idActual)) return false;
        return true;
    }
}