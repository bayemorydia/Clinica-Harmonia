package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private TraduccionService traduccionService;
    @Autowired
    private UsuarioService usuarioService;
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
    public String dashboard(Model model, Authentication auth) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());

        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("totalPacientes", citaService.countPacientesPendientesPorMedico(medico.getId()));
        model.addAttribute("citasHoy", citaService.countCitasHoy(medico.getId()));
        model.addAttribute("citasPendientes", citaService.obtenerCitasPendientesMedico(medico.getId()));

        return "Medico/dashboard";
    }

    @GetMapping("/paciente/historial/{pacienteId}")
    public String verHistorialClinico(
            @PathVariable Long pacienteId,
            @RequestParam(value = "citaId", required = false) Long citaId,
            @RequestParam(value = "fechaInicio", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model,
            Authentication auth) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        Paciente paciente = pacienteService.findPacienteById(pacienteId);

        // Lógica de filtrado movida al servicio
        HistorialClinico historial = consultaService.obtenerHistorialFiltrado(pacienteId, fechaInicio, fechaFin);

        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("historial", historial);
        model.addAttribute("citaIdOrigen", citaId);
        model.addAttribute("paciente", paciente);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "agenda");

        return "Medico/historial";
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal, Model model) {
        if (principal != null) {
            // El service encapsula la lógica de búsqueda y validación
            Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(principal.getName());

            if (empleado != null) {
                model.addAttribute("empleado", empleado);

                // Lógica de soporte movida a métodos privados o inyectada
                cargarListasSoportePerfil(model);

                model.addAttribute("trad", traduccionService);
                model.addAttribute("activePage", "perfil");
                return "empleado/detalle";
            }
        }
        return "redirect:/medico/dashboard";
    }

    // Método privado para limpiar el GetMapping
    private void cargarListasSoportePerfil(Model model) {
        model.addAttribute("roles", rolService.listarRoles(PageRequest.of(0, 200)).getContent());
        model.addAttribute("turnos", turnoService.listarTodos());
    }

    @GetMapping("/agenda")
    public String mostrarAgenda(Model model, Authentication auth,
            @RequestParam(name = "fecha", required = false) String fechaStr) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());

        // Lógica de parseo de fecha movida al servicio
        LocalDate fechaBusqueda = citaService.parsearFechaAgenda(fechaStr);
        List<Cita> citas = citaService.obtenerCitasDia(medico.getId(), fechaBusqueda);

        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("citas", citas);
        model.addAttribute("fechaActual", fechaBusqueda);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("activePage", "agenda");
        model.addAttribute("trad", traduccionService);

        return "Medico/agenda";
    }

    @GetMapping("/atender/{id}")
    public String atenderPaciente(@PathVariable("id") Long id, Model model, Authentication auth) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
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

    @GetMapping("/resumen-cita/{citaId}")
    public String verResumenCita(@PathVariable Long citaId, Model model, Authentication auth) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        Cita cita = citaService.findCitaById(citaId);

        // Lógica de búsqueda de episodio específico movida al servicio
        EpisodioClinico episodio = consultaService.buscarEpisodioPorCita(cita);

        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("cita", cita);
        model.addAttribute("episodio", episodio);
        model.addAttribute("paciente", cita.getPaciente());
        model.addAttribute("trad", traduccionService);

        return "Medico/resumenCita";
    }

    @PostMapping("/finalizar-consulta")
    public String guardarConsulta(@RequestParam("citaId") Long citaId,
            @RequestParam("diagnosticoId") Long diagnosticoId,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(value = "medIds[]", required = false) List<Long> medIds,
            @RequestParam(value = "medDosis[]", required = false) List<String> medDosis,
            @RequestParam(value = "pruebaIds[]", required = false) List<Long> pruebaIds) {

        // Lógica de preparación de listas y guardado movida al servicio
        consultaService.procesarFinalizacionConsulta(citaId, diagnosticoId, tratamiento, medIds, medDosis, pruebaIds);

        return "redirect:/medico/agenda?success=true";
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

    @GetMapping("/nominas")
    public String listarMisNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        Page<Nomina> paginaNominas = nominaService.getNominasPorEmpleado(medico.getId(), PageRequest.of(page, size));

        model.addAttribute("empleado", medico); // <--- IMPORTANTE: Usar "empleado" para que el HTML lo detecte
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Empleado/nominasEmpleado"; // Tu vista específica
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());

        // Usamos el nuevo método del service que filtra por empleado y periodo
        // (mes/año)
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(medico.getId(), query, page, size);

        model.addAttribute("empleado", medico);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query); // Mantener el valor en el input
        model.addAttribute("trad", traduccionService);

        return "Empleado/nominasEmpleado :: tablaNominas";
    }

    @PostMapping("/atender/finalizar")
    public String finalizarConsultaLegacy(@RequestParam Long citaId,
            @ModelAttribute EpisodioClinico episodio,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(required = false) List<Long> pruebasIds,
            @RequestParam(required = false) List<String> medNombre,
            @RequestParam(required = false) List<String> medDosis,
            @RequestParam(required = false) List<Double> medPrecio) {

        // Lógica delegada al servicio para mantener compatibilidad con el método
        // duplicado
        consultaService.procesarConsultaLegacy(citaId, episodio, tratamiento, pruebasIds, medNombre, medDosis);

        return "redirect:/medico/dashboard?success";
    }
}