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
import cat.copernic.proyecto2.DAO.PacienteDAO;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Imagen.ImagenService;
import cat.copernic.proyecto2.Service.Validaciones.ValidacionesService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService; // Importación nueva
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
    private UsuarioService usuarioService; // Inyectado para crear la cuenta de acceso

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
            // --- Lógica para NUEVO paciente ---
            validacionesService.validarDocumentoIdentidad(paciente.getDni());
            validarDuplicados(paciente);
            
            paciente.setFechaAlta(LocalDate.now());

            // 1. Crear automáticamente el usuario de acceso al sistema
            Usuario nuevoUsuario = usuarioService.crearUsuarioPaciente(
                paciente.getNombre(), 
                paciente.getApellidos(), 
                paciente.getDni()
            );
            
            // 2. Vincular el usuario creado con el paciente
            paciente.setUsuario(nuevoUsuario);

            if (nombreImagenNueva != null) {
                paciente.setRutaFoto(nombreImagenNueva);
            } else {
                paciente.setRutaFoto("default-user.png");
            }
        } else {
            // --- Lógica para EDITAR paciente ---
            Paciente existente = pacienteDAO.findById(paciente.getId()).orElse(null);
            if (existente != null) {
                paciente.setFechaAlta(existente.getFechaAlta());
                paciente.setUsuario(existente.getUsuario()); // Mantener el usuario actual

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
        if (pacienteDAO.existsByDni(paciente.getDni())) {
            throw new RuntimeException("El DNI/NIE ya está registrado en el sistema");
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
            p.setActivo(false);
            // También desactivamos su acceso al sistema si tiene usuario
            if (p.getUsuario() != null) {
                p.getUsuario().setActivo(false);
            }
            pacienteDAO.save(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        Period periodo = Period.between(paciente.getFechaNacimiento(), LocalDate.now());
        if (periodo.getYears() >= 1) {
            return periodo.getYears() + ":" + (periodo.getYears() == 1 ? "año" : "años");
        }
        if (periodo.getMonths() >= 1) {
            return periodo.getMonths() + ":" + (periodo.getMonths() == 1 ? "mes" : "meses");
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(paciente.getFechaNacimiento(), LocalDate.now());
        if (dias <= 0) return "0:recien_nacido";
        return dias + ":" + (dias == 1 ? "dia" : "dias");
    }

    @Override
    public long countPacientesActivos() {
        return pacienteDAO.countByActivoTrue();
    }

    @Override
    public Page<Paciente> getPacientesPaginados(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending());
        if (query == null || query.isEmpty()) {
            return pacienteDAO.findAll(pageable);
        }
        return pacienteDAO.findByNombreOrDni(query, pageable);
    }
}