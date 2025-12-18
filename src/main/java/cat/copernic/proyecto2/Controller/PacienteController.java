package cat.copernic.proyecto2.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.validation.Valid;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping("")
    public String listarPacientes(Model model) {
        List<Paciente> pacientes = pacienteService.getAllPacientes();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("btnAnadirPaciente", "Añadir Nuevo Paciente");
        model.addAttribute("pacienteService", pacienteService);
        return "Paciente/listaPacientes";
    }

    @GetMapping("/anadir")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("titulo", "Registrar Nuevo Paciente");
        return "Paciente/formularioPaciente";
    }

    @GetMapping("/activar/{id}")
    public String activarPaciente(@PathVariable("id") Long id) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente != null) {
            paciente.setActivo(true); // Cambiamos el estado a activo
            pacienteService.savePaciente(paciente, null); // Guardamos (enviamos null a la foto según tu lógica)
        }
        return "redirect:/pacientes"; // Redirigimos de nuevo al listado
    }

    @PostMapping("/guardar")
    public String guardarPaciente(
            @Valid @ModelAttribute("paciente") Paciente paciente,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile imagen,
            Model model) {
        if (result.hasErrors()) {
            return "Paciente/formularioPaciente";
        }
        try {
            pacienteService.savePaciente(paciente, imagen);
            return "redirect:/pacientes";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            return "Paciente/formularioPaciente";
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetallePaciente(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("pacienteService", pacienteService);
        model.addAttribute("titulo", "Detalles del Paciente");
        return "Paciente/detallePaciente";
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String confirmarEliminar(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("titulo", "Confirmar Desactivación");
        return "Paciente/confirmarEliminarPaciente";
    }

    // Cambiado: Ahora desactiva en lugar de borrar
    @PostMapping("/eliminar")
    public String desactivarPaciente(@RequestParam("id") Long id) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente != null) {
            paciente.setActivo(false); // Cambiamos el estado a falso
            pacienteService.savePaciente(paciente, null); // Guardamos el cambio
        }
        return "redirect:/pacientes";
    }
}