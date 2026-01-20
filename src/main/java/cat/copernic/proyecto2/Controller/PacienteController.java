package cat.copernic.proyecto2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    private TraduccionService traduccionService;

    @GetMapping("")
    public String listarPacientes(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Paciente> pacientePage = pacienteService.getPacientesPaginados(query, activo, page, size);
        model.addAttribute("pacientes", pacientePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pacientePage.getTotalPages());
        model.addAttribute("totalElements", pacientePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Paciente/listaPacientes";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Paciente> pacientePage = pacienteService.getPacientesPaginados(query, activo, page, size);
        model.addAttribute("pacientes", pacientePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pacientePage.getTotalPages());
        model.addAttribute("totalElements", pacientePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Paciente/listaPacientes :: tablaPacientes";
    }

    @GetMapping("/anadir")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("trad", traduccionService);
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
                Paciente pacienteExistente = pacienteService.findPacienteById(paciente.getId());
                if (pacienteExistente != null) {
                    model.addAttribute("edad", pacienteService.getEdadPaciente(pacienteExistente));
                    paciente.setUsuario(pacienteExistente.getUsuario());
                    paciente.setFechaNacimiento(pacienteExistente.getFechaNacimiento());
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
                Paciente pacienteExistente = pacienteService.findPacienteById(paciente.getId());
                if (pacienteExistente != null) {
                    model.addAttribute("edad", pacienteService.getEdadPaciente(pacienteExistente));
                    paciente.setUsuario(pacienteExistente.getUsuario());
                    paciente.setFechaAlta(pacienteExistente.getFechaAlta());
                    paciente.setFechaNacimiento(pacienteExistente.getFechaNacimiento());
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
        model.addAttribute("trad", traduccionService);
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
}