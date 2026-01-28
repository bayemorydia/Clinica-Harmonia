package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Prueba.PruebaService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Consulta.ConsultaService;
import cat.copernic.proyecto2.Service.Diagnostico.DiagnosticoService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private TraduccionService traduccionService;
    @Autowired
    private ConsultaService consultaService;
    @Autowired
    private CitaService citaService;
    @Autowired
    private EmpleadoService empleadoService;
    @Autowired
    private NominaService nominaService;
    @Autowired
    private MedicamentoService medicamentoService;
    @Autowired
    private PruebaService pruebaService;
    @Autowired
    private DiagnosticoService diagnosticoService;
    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private RolService rolService;
    @Autowired
    private TurnoService turnoService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("totalPacientes", citaService.countPacientesPendientesPorMedico(medico.getId()));
        model.addAttribute("citasHoy", citaService.countCitasHoy(medico.getId()));
        model.addAttribute("citasPendientes", citaService.obtenerCitasPendientesMedico(medico.getId()));
        return "Medico/dashboard";
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal, Model model) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("empleado", medico);
        model.addAttribute("roles", rolService.listarTodos());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");
        return "Empleado/detalle";
    }

    @GetMapping("/nominas")
    public String listarMisNominas(Model model, Principal principal, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String query) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(medico.getId(), query, page, size);
        model.addAttribute("empleado", medico);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Empleado/nominasEmpleado";
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(medico.getId(), query, page, size);
        model.addAttribute("empleado", medico);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        return "Empleado/nominasEmpleado :: tablaNominas";
    }

    @GetMapping("/agenda")
    public String mostrarAgenda(Model model, Principal principal,
            @RequestParam(name = "fecha", required = false) String fechaStr) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        LocalDate fechaBusqueda = citaService.parsearFechaAgenda(fechaStr);
        List<Cita> citas = citaService.obtenerCitasDia(medico.getId(), fechaBusqueda);
        Map<String, Long> resumen = citaService.obtenerResumenCitas(citas);
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("citas", citas);
        model.addAttribute("totalCitas", resumen.get("total"));
        model.addAttribute("citasProgramadas", resumen.get("programadas"));
        model.addAttribute("citasRealizadas", resumen.get("realizadas"));
        model.addAttribute("fechaActual", fechaBusqueda);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("activePage", "agenda");
        model.addAttribute("trad", traduccionService);
        return "Medico/agenda";
    }

    @GetMapping("/atender/{id}")
    public String atenderPaciente(@PathVariable("id") Long id, Model model, Principal principal) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        Cita cita = citaService.findCitaById(id);
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", cita.getPaciente());
        model.addAttribute("listaMedicamentos", medicamentoService.listarMedicamentos());
        model.addAttribute("listaPruebas", pruebaService.listarPruebas());
        model.addAttribute("listaDiagnosticos", diagnosticoService.listarTodos());
        model.addAttribute("episodio", new EpisodioClinico());
        model.addAttribute("tratamiento", new Tratamiento());
        model.addAttribute("trad", traduccionService);
        return "Medico/atender";
    }

    @GetMapping("/cita/no-asistio/{id}")
    public String marcarNoAsistio(@PathVariable Long id, RedirectAttributes flash) {
        try {
            consultaService.cambiarEstadoCita(id, EstadoCita.NO_PRESENTADO);
            flash.addFlashAttribute("warning", "La cita ha sido marcada como 'No asistió'.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar la cita.");
        }
        return "redirect:/medico/agenda";
    }

    @PostMapping("/finalizar-consulta")
    public String guardarConsulta(@RequestParam("citaId") Long citaId,
            @RequestParam("diagnosticoId") Long diagnosticoId,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(value = "medIds[]", required = false) List<Long> medIds,
            @RequestParam(value = "medDosis[]", required = false) List<String> medDosis,
            @RequestParam(value = "pruebaIds[]", required = false) List<Long> pruebaIds) {
        consultaService.procesarFinalizacionConsulta(citaId, diagnosticoId, tratamiento, medIds, medDosis, pruebaIds);
        return "redirect:/medico/agenda?success=true";
    }

    @GetMapping("/paciente/historial/{pacienteId}")
    public String verHistorialClinico(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) Long citaId,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fechaFin,
            Model model,
            Principal principal) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        Paciente paciente = pacienteService.findPacienteById(pacienteId);
        HistorialClinico historial = consultaService.obtenerHistorialFiltrado(pacienteId, fechaInicio, fechaFin);
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("paciente", paciente);
        model.addAttribute("historial", historial);
        model.addAttribute("citaIdOrigen", citaId);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "agenda");
        return "Medico/historial";
    }

    @GetMapping("/resumen-cita/{citaId}")
    public String verResumenCita(@PathVariable Long citaId, Model model, Principal principal) {
        Empleado medico = empleadoService.obtenerPorUsername(principal.getName());
        Cita cita = citaService.findCitaById(citaId);
        EpisodioClinico episodio = consultaService.buscarEpisodioPorCita(cita);
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("cita", cita);
        model.addAttribute("episodio", episodio);
        model.addAttribute("paciente", cita.getPaciente());
        model.addAttribute("trad", traduccionService);
        return "Medico/resumenCita";
    }
}