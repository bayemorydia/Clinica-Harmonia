package cat.copernic.proyecto2.Service.Paciente;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.DAO.PacienteDAO;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Imagen.ImagenService;
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
        if (paciente.getDni() != null) {
            paciente.setDni(paciente.getDni().toUpperCase().trim());
        }
        if (paciente.getId() == null || paciente.getId() == 0) {
            paciente.setFechaAlta(LocalDate.now());
            validarDocumentoIdentidad(paciente.getDni());
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
                if (!existente.getDni().equals(paciente.getDni())) {
                    validarDocumentoIdentidad(paciente.getDni());
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
            pacienteDAO.save(p);
        }
    }

    @Override
    public String getEdadPaciente(Paciente paciente) {
        Period periodo = Period.between(paciente.getFechaNacimiento(), LocalDate.now());

        if (periodo.getYears() >= 1) {
            // Devolvemos: "25:años"
            return periodo.getYears() + ":" + (periodo.getYears() == 1 ? "año" : "años");
        }
        if (periodo.getMonths() >= 1) {
            return periodo.getMonths() + ":" + (periodo.getMonths() == 1 ? "mes" : "meses");
        }

        long dias = java.time.temporal.ChronoUnit.DAYS.between(paciente.getFechaNacimiento(), LocalDate.now());
        if (dias <= 0)
            return "0:recien_nacido";

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

    private void validarDocumentoIdentidad(String documento) {
        if (documento == null || documento.isEmpty()) {
            throw new RuntimeException("El documento de identidad es obligatorio");
        }
        Pattern pattern = Pattern.compile("(\\d{8}[A-Z]|[XYZ]\\d{7}[A-Z])");
        if (!pattern.matcher(documento).matches()) {
            throw new RuntimeException("Formato de DNI o NIE inválido");
        }
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        String nifParaCalculo = documento;
        if (documento.startsWith("X"))
            nifParaCalculo = nifParaCalculo.replace("X", "0");
        else if (documento.startsWith("Y"))
            nifParaCalculo = nifParaCalculo.replace("Y", "1");
        else if (documento.startsWith("Z"))
            nifParaCalculo = nifParaCalculo.replace("Z", "2");
        int numero = Integer.parseInt(nifParaCalculo.substring(0, 8));
        char letraEsperada = letras.charAt(numero % 23);
        char letraProporcionada = documento.charAt(8);
        if (letraEsperada != letraProporcionada) {
            throw new RuntimeException("La letra del DNI/NIE no es correcta. Debería ser: " + letraEsperada);
        }
    }
}
