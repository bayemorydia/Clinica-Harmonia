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
            paciente.setActivo(true); // Cambiamos a true
            pacienteService.savePaciente(paciente, null); // Ahora el Service sí guardará este true
        }
        // Redirigimos a la lista de activos para ver el resultado
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
                model.addAttribute("edad", pacienteService.getEdadPaciente(paciente));
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
        } catch (RuntimeException e) {
            if ("DNI_DUPLICADO".equals(e.getMessage())) {
                model.addAttribute("error", traduccionService.getTraduccion("paciente.valid.dni_existe"));
            } else {
                model.addAttribute("error", e.getMessage());
            }
            return "Paciente/formularioPaciente";
        }
    }

    // MÉTODO EXCLUSIVO PARA ACTUALIZAR LA FOTO DESDE DETALLE
    @PostMapping("/actualizar-foto")
    public String actualizarFoto(@RequestParam("id") Long id, @RequestParam("file") MultipartFile file) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente != null && file != null && !file.isEmpty()) {
            pacienteService.savePaciente(paciente, file);
        }
        return "redirect:/pacientes/detalle/" + id + "?success&t=" + System.currentTimeMillis();
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
    public String confirmarDesactivar(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null)
            return "redirect:/pacientes";
        model.addAttribute("paciente", paciente);
        model.addAttribute("trad", traduccionService);
        return "Paciente/confirmarEliminarPaciente";
    }

    @PostMapping("/eliminar")
    public String desactivarPaciente1(@RequestParam("id") Long id) {
        pacienteService.desactivarPaciente(id);
        return "redirect:/pacientes";
    }
}