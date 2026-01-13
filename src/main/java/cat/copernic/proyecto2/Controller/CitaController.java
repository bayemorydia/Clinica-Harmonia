package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    private void cargarListasFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        model.addAttribute("profesionales", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
    }

    @GetMapping("")
    public String listarCitas(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
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

        // Si la petición viene de JS (AJAX), devolvemos solo el fragmento
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "Cita/listaCitas :: tablaCitas";
        }

        // Si no, devolvemos la página completa
        return "Cita/listaCitas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Cita nuevaCita = new Cita();
        // Próxima hora disponible
        nuevaCita.setFechaHora(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1));

        model.addAttribute("cita", nuevaCita);
        cargarListasFormulario(model);
        return "Cita/formularioCita";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {

        if (result.hasErrors()) {
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }

        try {
            // Evitamos editar si tu lógica de negocio lo prohíbe explícitamente
            if (cita.getId() != null) {
                // Aquí podrías permitir editar o no. Si no permites:
                // flash.addFlashAttribute("error", "No se permite editar.");
                // return "redirect:/citas";
            }

            citaService.saveCita(cita);
            flash.addFlashAttribute("success", "Cita procesada con éxito");
            return "redirect:/citas";

        } catch (Exception e) {
            model.addAttribute("errorNegocio", e.getMessage());
            cargarListasFormulario(model);
            // Devolvemos el mismo objeto cita para que no pierda los datos introducidos,
            // pero el Service ya se encargó de normalizar la fecha si fue necesario.
            return "Cita/formularioCita";
        }
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String mostrarConfirmacionAnular(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);

        if (cita == null) {
            return "redirect:/citas";
        }

        // Si la cita ya está anulada, no tiene sentido confirmar nada, volvemos a la
        // lista
        if ("ANULADA".equals(cita.getEstado().name())) {
            return "redirect:/citas";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        return "Cita/confirmarEliminarCita";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null)
            return "redirect:/citas";

        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("readOnly", true);
        return "Cita/detalleCita";
    }

    @PostMapping("/anular")
    public String anularCita(@RequestParam("id") Long id, RedirectAttributes flash) {
        citaService.anularCita(id);
        flash.addFlashAttribute("success", "Cita anulada correctamente");
        return "redirect:/citas";
    }

    @InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.registerCustomEditor(Paciente.class, new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty())
                    return;
                setValue(pacienteService.findPacienteById(Long.parseLong(text)));
            }
        });
        binder.registerCustomEditor(Empleado.class, new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty())
                    return;
                setValue(empleadoService.obtenerPorId(Long.parseLong(text)));
            }
        });
    }
}