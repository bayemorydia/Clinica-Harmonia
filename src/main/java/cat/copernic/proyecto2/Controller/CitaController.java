package cat.copernic.proyecto2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    // LISTADO DE CITAS
    @GetMapping("")
    public String listarCitas(Model model) {
        List<Cita> citas = citaService.getAllCitas();
        model.addAttribute("citas", citas);
        return "Cita/listaCitas"; // Debes crear esta vista
    }

    // FORMULARIO PARA NUEVA CITA
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("cita", new Cita());
        // Cargamos pacientes para el select
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        // El médico se escribirá manualmente en el input text del HTML
        return "Cita/formularioCita";
    }

    // FORMULARIO PARA EDITAR CITA
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null)
            return "redirect:/citas";

        model.addAttribute("cita", cita);
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        return "Cita/formularioCita";
    }

    // GUARDAR O ACTUALIZAR
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.getAllPacientes());
            return "Cita/formularioCita";
        }

        try {
            // Lógica de negocio (horarios, solapamientos) ejecutada en ServiceImpl
            citaService.saveCita(cita);
            return "redirect:/citas";

        } catch (Exception e) {
            // Captura los errores de ServiceImpl (ej: "Médico ocupado") y los envía a la UI
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("pacientes", pacienteService.getAllPacientes());
            return "Cita/formularioCita";
        }
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String confirmarEliminar(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null) {
            return "redirect:/citas";
        }
        model.addAttribute("cita", cita);
        return "Cita/confirmarEliminarCita";
    }

    // Ejecuta la eliminación definitiva (vía POST)
    @PostMapping("/eliminar")
    public String eliminarCita(@RequestParam("id") Long id) {
        citaService.deleteCita(id);
        return "redirect:/citas";
    }
}