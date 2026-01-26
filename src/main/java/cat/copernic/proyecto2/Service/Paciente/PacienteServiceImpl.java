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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteDAO pacienteDAO;

    @Autowired
    private ImagenService imagenService;

    @Autowired(required = false)
    private ValidacionesService validacionesService;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Paciente> getAllPacientes() {
        return pacienteDAO.findAll();
    }

    @Override
    @Transactional
    public void savePaciente(Paciente pacienteForm, MultipartFile file) {
        if (pacienteForm.getDni() != null) {
            pacienteForm.setDni(pacienteForm.getDni().toUpperCase().trim());
        }

        if (pacienteForm.getId() == null || pacienteForm.getId() == 0) {
            // Lógica de Nuevo Paciente
            if (validacionesService != null) {
                validacionesService.validarDocumentoIdentidad(pacienteForm.getDni());
            }
            if (pacienteDAO.existsByDni(pacienteForm.getDni())) {
                throw new RuntimeException("DNI_DUPLICADO");
            }

            pacienteForm.setFechaAlta(LocalDate.now());
            pacienteForm.setActivo(true);

            Usuario nuevoUsuario = usuarioService.crearUsuarioPaciente(
                    pacienteForm.getNombre(), pacienteForm.getApellidos(), pacienteForm.getDni());
            pacienteForm.setUsuario(nuevoUsuario);

            String nombreImagen = imagenService.guardarImagen(file, "Paciente");
            pacienteForm.setRutaFoto(nombreImagen);

            pacienteDAO.save(pacienteForm);
        } else {
            // Lógica de Edición
            Paciente pacienteBD = pacienteDAO.findById(pacienteForm.getId())
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            pacienteBD.setNombre(pacienteForm.getNombre());
            pacienteBD.setApellidos(pacienteForm.getApellidos());
            pacienteBD.setFechaNacimiento(pacienteForm.getFechaNacimiento());
            pacienteBD.setTelefono(pacienteForm.getTelefono());
            pacienteBD.setEmail(pacienteForm.getEmail());
            pacienteBD.setDireccion(pacienteForm.getDireccion());
            pacienteBD.setActivo(pacienteForm.isActivo());

            if (file != null && !file.isEmpty()) {
                gestionarCambioImagen(pacienteBD, file);
            }
            pacienteDAO.save(pacienteBD);
        }
    }

    @Override
    @Transactional
    public void actualizarFotoDetalle(Long id, MultipartFile file) {
        Paciente paciente = findPacienteById(id);
        if (paciente != null && file != null && !file.isEmpty()) {
            gestionarCambioImagen(paciente, file);
            pacienteDAO.save(paciente);
        }
    }

    private void gestionarCambioImagen(Paciente paciente, MultipartFile file) {
        if (paciente.getRutaFoto() != null && !paciente.getRutaFoto().equals("default-user.png")) {
            imagenService.eliminarImagen(paciente.getRutaFoto(), "Paciente");
        }
        paciente.setRutaFoto(imagenService.guardarImagen(file, "Paciente"));
    }

    @Override
    public Paciente findPacienteById(Long id) {
        return pacienteDAO.findById(id).orElse(null);
    }

    @Override
    public Paciente encontrarPorDni(String dni) {
        return pacienteDAO.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con DNI: " + dni));
    }

    @Override
    @Transactional
    public void desactivarPaciente(Long id) {
        Paciente p = findPacienteById(id);
        if (p != null) {
            p.setActivo(false);
            pacienteDAO.save(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        if (paciente == null || paciente.getFechaNacimiento() == null)
            return "N/D";
        return Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears() + " años";
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

    @Override
    public boolean existeDni(String dni, Long idActual) {
        return pacienteDAO.findByDni(dni)
                .map(encontrado -> idActual == null || !encontrado.getId().equals(idActual))
                .orElse(false);
    }

    @Override
    public Paciente buscarPorUsername(String username) {
        return pacienteDAO.findByUsuarioUsername(username).orElse(null);
    }

    @Override
    @Transactional
    public void activarPaciente(Long id) {
        Paciente paciente = findPacienteById(id);
        if (paciente != null) {
            paciente.setActivo(true);
            pacienteDAO.save(paciente);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long obtenerTotalPacientes() {
        return pacienteDAO.count();
    }
}