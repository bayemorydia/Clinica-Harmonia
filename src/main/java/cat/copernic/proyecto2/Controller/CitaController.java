package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Controlador para la gestión de citas médicas.
 * Gestión profesional de errores y renderizado de formularios.
 */
@Controller
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final EmpleadoService empleadoService;
    private final TraduccionService traduccionService;

    /**
     * Carga las listas necesarias para los combos de Pacientes y Profesionales.
     */
    private void cargarListasFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        model.addAttribute("profesionales", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
    }

    /**
     * Prepara una instancia limpia de Cita con la hora configurada por defecto.
     */
    private Cita crearCitaPorDefecto() {
        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1));
        return cita;
    }

    @GetMapping
    public String listarCitas(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        Page<Cita> paginaCitas = citaService.getCitasPaginadas(query, page, size);
        model.addAttribute("citas", paginaCitas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaCitas.getTotalPages());
        model.addAttribute("totalElements", paginaCitas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        //Devuelve lo del buscador
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "Cita/listaCitas :: tablaCitas";
        }
        //Devuelve listado completo
        return "Cita/listaCitas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("cita", crearCitaPorDefecto());
        cargarListasFormulario(model);
        return "Cita/formularioCita";
    }
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        // Error de validación de campos (jakarta.validation)
        if (result.hasErrors()) {
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }
        try {
            citaService.saveCita(cita);
            flash.addFlashAttribute("success", "Cita procesada con éxito");
            return "redirect:/citas";
        } catch (Exception e) {
            model.addAttribute("cita", crearCitaPorDefecto());
            model.addAttribute("errorNegocio", e.getMessage());
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String mostrarConfirmacionAnular(@PathVariable Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null || "ANULADA".equals(cita.getEstado().name())) {
            return "redirect:/citas";
        }
        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        return "Cita/confirmarEliminarCita";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null) {
            return "redirect:/citas";
        }
        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("readOnly", true);
        return "Cita/detalleCita";
    }

    @PostMapping("/anular")
    public String anularCita(@RequestParam Long id, RedirectAttributes flash) {
        citaService.anularCita(id);
        flash.addFlashAttribute("success", "Cita anulada correctamente");
        return "redirect:/citas";
    }
}