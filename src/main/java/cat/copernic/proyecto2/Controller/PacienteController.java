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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.PacienteService;
import jakarta.validation.Valid;

@Controller
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping("/pacientes")
    public String listarPacientes(Model model) {
        List<Paciente> pacientes = pacienteService.getAllPacientes();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("btnAnadirPaciente", "Añadir Nuevo Paciente");
        model.addAttribute("headerAccion", "Acción");
        model.addAttribute("btnUpdate", "Editar");
        model.addAttribute("btnDelete", "Eliminar");
        model.addAttribute("pacienteService", pacienteService);
        return "Paciente/listaPacientes";
    }

    @GetMapping("/pacientes/anadir")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("titulo", "Registrar Nuevo Paciente");
        return "Paciente/formularioPaciente";
    }

    @PostMapping("/pacientes/guardar")
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

    @GetMapping("/pacientes/detalle/{id}")
    public String verDetallePaciente(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("pacienteService", pacienteService);
        model.addAttribute("titulo", "Detalles del Paciente: " + paciente.getNombre() + " " + paciente.getApellidos());
        return "Paciente/detallePaciente";
    }

    @GetMapping("/pacientes/confirmarEliminar/{id}")
    public String confirmarEliminar(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("titulo", "Confirmar Eliminación");
        return "Paciente/confirmarEliminarPaciente";
    }

    @PostMapping("/pacientes/eliminar")
    public String eliminarPaciente(@RequestParam("id") Long id) {
        pacienteService.deletePaciente(id);
        return "redirect:/pacientes";
    }

}
