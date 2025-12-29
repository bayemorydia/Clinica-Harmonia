package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TraduccionService traduccionService;

    /**
     * Carga los datos necesarios para los desplegables del formulario.
     */
    private void cargarListasFormulario(Model model) {
        // Obtenemos todos los pacientes
        model.addAttribute("pacientes", pacienteService.getAllPacientes());

        // Obtenemos TODOS los empleados (sin filtrar por activos)
        model.addAttribute("empleados", empleadoService.listarTodos());

        model.addAttribute("trad", traduccionService);
    }

    @GetMapping("")
    public String listarCitas(@RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {

        Page<Cita> paginaCitas = citaService.getCitasPaginadas(query, page, size);

        model.addAttribute("citas", paginaCitas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaCitas.getTotalPages());
        model.addAttribute("totalElements", paginaCitas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);

        return "Cita/listaCitas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("cita", new Cita());
        cargarListasFormulario(model);
        return "Cita/formularioCita";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null)
            return "redirect:/citas";

        model.addAttribute("cita", cita);
        cargarListasFormulario(model);
        return "Cita/formularioCita";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }

        try {
            citaService.saveCita(cita);
            return "redirect:/citas";
        } catch (Exception e) {
            model.addAttribute("errorNegocio", e.getMessage());
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String confirmarAnular(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null)
            return "redirect:/citas";

        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        return "Cita/confirmarEliminarCita"; // Nombre del archivo HTML
    }

    @PostMapping("/anular")
    public String anularCita(@RequestParam("id") Long id) {
        citaService.anularCita(id);
        return "redirect:/citas";
    }
}