package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Especialidad; // Añadido
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Cita.FacturaPdfService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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

    @Autowired
    private FacturaPdfService facturaPdfService;

    private void cargarListasFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        // El Controller ya no filtra, el Service le da los médicos listos
        model.addAttribute("profesionales", citaService.listarMedicosActivos());
        model.addAttribute("estadosCita", EstadoCita.values());
        model.addAttribute("especialidades", Especialidad.values());
        model.addAttribute("trad", traduccionService);
    }

    @GetMapping
    public String listarCitas(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "estado", required = false) EstadoCita estado,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        Page<Cita> paginaCitas = citaService.getCitasPaginadas(query, estado, page, size);
        model.addAttribute("citas", paginaCitas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaCitas.getTotalPages());
        model.addAttribute("totalElements", paginaCitas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estadosCita", EstadoCita.values());
        model.addAttribute("trad", traduccionService);
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "Cita/listaCitas :: tablaCitas";
        }
        return "Cita/listaCitas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Cita cita = new Cita();
        // Sugerir la próxima hora en punto disponible
        cita.setFechaHora(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1));
        model.addAttribute("cita", cita);
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
            citaService.saveCita(cita);
            flash.addFlashAttribute("success", "Cita procesada con éxito");
            return "redirect:/citas";
        } catch (Exception e) {
            model.addAttribute("errorNegocio", e.getMessage());
            cargarListasFormulario(model);
            return "Cita/formularioCita";
        }
    }

    @GetMapping("/confirmarEliminar/{id}")
    public String mostrarConfirmacionAnular(@PathVariable Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null || cita.getEstado() != EstadoCita.PROGRAMADA) {
            return "redirect:/citas";
        }
        model.addAttribute("cita", cita);
        model.addAttribute("trad", traduccionService);
        return "Cita/confirmarEliminarCita";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Cita cita = citaService.findCitaById(id);
        if (cita == null)
            return "redirect:/citas";
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

    @GetMapping("/factura/{id}")
    public void descargarFactura(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        Cita cita = citaService.findCitaById(id);
        if (cita != null) {
            response.setContentType("application/pdf");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Factura_Cita_" + id + "_" + currentDateTime + ".pdf";
            response.setHeader(headerKey, headerValue);
            facturaPdfService.exportar(response, cita);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        // Registramos editores para que Spring sepa convertir los IDs de los selects en
        // objetos reales
        binder.registerCustomEditor(Paciente.class, new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty())
                    return;
                setValue(pacienteService.findPacienteById(Long.parseLong(text)));
            }
        });
        binder.registerCustomEditor(Empleado.class, new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty())
                    return;
                setValue(empleadoService.obtenerPorId(Long.parseLong(text)));
            }
        });
    }
}