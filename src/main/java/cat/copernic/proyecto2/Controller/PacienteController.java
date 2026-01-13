package cat.copernic.proyecto2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private TraduccionService traduccionService; // Inyección de traducciones

    @GetMapping("")
    public String listarPacientes(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Paciente> pacientePage = pacienteService.getPacientesPaginados(query, page, size);
        model.addAttribute("pacientes", pacientePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pacientePage.getTotalPages());
        model.addAttribute("totalElements", pacientePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        return "Paciente/listaPacientes";
    }

    @GetMapping("/anadir")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("trad", traduccionService); // Pasar traducciones
        return "Paciente/formularioPaciente";
    }

    @GetMapping("/activar/{id}")
    public String activarPaciente(@PathVariable("id") Long id) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente != null) {
            paciente.setActivo(true);
            pacienteService.savePaciente(paciente, null);
        }
        return "redirect:/pacientes";
    }

    @PostMapping("/guardar")
    public String guardarPaciente(
            @Valid @ModelAttribute("paciente") Paciente paciente,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile imagen,
            @RequestParam(value = "origen", required = false) String origen,
            Model model) {

        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            if ("detalle".equals(origen)) {
                // Recuperamos el paciente de la DB para asegurar que tenemos la edad correcta
                Paciente pacienteExistente = pacienteService.findPacienteById(paciente.getId());
                if (pacienteExistente != null) {
                    model.addAttribute("edad", pacienteService.getEdadPaciente(pacienteExistente));
                    // Opcional: restauramos la fecha en el objeto actual para evitar NPE en la
                    // vista
                    paciente.setFechaNacimiento(pacienteExistente.getFechaNacimiento());
                    paciente.setUsuario(pacienteExistente.getUsuario());
                }
                return "Paciente/detallePaciente";
            }
            return "Paciente/formularioPaciente";
        }

        try {
            pacienteService.savePaciente(paciente, imagen);

            if ("detalle".equals(origen)) {
                return "redirect:/pacientes/detalle/" + paciente.getId() + "?success";
            }
            return "redirect:/pacientes";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());

            if ("detalle".equals(origen)) {
                // En caso de excepción (ej. DNI duplicado), volvemos a cargar los datos
                // necesarios para la vista
                Paciente pacienteExistente = pacienteService.findPacienteById(paciente.getId());
                if (pacienteExistente != null) {
                    model.addAttribute("edad", pacienteService.getEdadPaciente(pacienteExistente));
                    paciente.setFechaNacimiento(pacienteExistente.getFechaNacimiento());
                    paciente.setUsuario(pacienteExistente.getUsuario());
                }
                return "Paciente/detallePaciente";
            }
            return "Paciente/formularioPaciente";
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetallePaciente(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null)
            return "redirect:/pacientes";
        model.addAttribute("paciente", paciente);
        model.addAttribute("edad", pacienteService.getEdadPaciente(paciente));
        model.addAttribute("trad", traduccionService);
        return "Paciente/detallePaciente";
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String confirmarEliminar(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null)
            return "redirect:/pacientes";

        model.addAttribute("paciente", paciente);
        model.addAttribute("trad", traduccionService); // Pasar traducciones
        return "Paciente/confirmarEliminarPaciente";
    }

    @PostMapping("/eliminar")
    public String desactivarPaciente(@RequestParam("id") Long id) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente != null) {
            paciente.setActivo(false);
            pacienteService.savePaciente(paciente, null);
        }
        return "redirect:/pacientes";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Paciente> pacientePage = pacienteService.getPacientesPaginados(query, page, size);
        model.addAttribute("pacientes", pacientePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pacientePage.getTotalPages());
        model.addAttribute("totalElements", pacientePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "Paciente/listaPacientes :: tablaPacientes";
    }
}