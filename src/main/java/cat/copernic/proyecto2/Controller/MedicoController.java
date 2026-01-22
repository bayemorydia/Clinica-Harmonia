package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Diagnostico;
import cat.copernic.proyecto2.Domain.PersonalSanitario.EpisodioClinico;
import cat.copernic.proyecto2.Domain.PersonalSanitario.HistorialClinico;
import cat.copernic.proyecto2.Domain.PersonalSanitario.LineaReceta;
import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Prueba.PruebaService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Consulta.ConsultaService;
import cat.copernic.proyecto2.Service.Diagnostico.DiagnosticoService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("medicoLogueado", medico);
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
        model.addAttribute("totalPacientes", citaService.countPacientesPendientesPorMedico(medico.getId()));
        model.addAttribute("citasHoy", citaService.countCitasHoyPorMedico(medico.getId(), inicioHoy, finHoy));
        model.addAttribute("citasPendientes", citaService.obtenerCitasPendientesMedico(medico.getId()));
        return "Medico/dashboard";
    }

    @GetMapping("/paciente/historial/{pacienteId}")
public String verHistorialClinico(
        @PathVariable Long pacienteId,
        @RequestParam(value = "citaId", required = false) Long citaId, // Recibimos el ID de la cita
        @RequestParam(value = "fechaInicio", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaInicio,
        @RequestParam(value = "fechaFin", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaFin,
        Model model, 
        Authentication auth) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());

        // 1. Obtenemos el historial completo (el Service ya hace la hidratación de
        // datos Lazy)
        HistorialClinico historial = consultaService.obtenerOSolucionarHistorial(pacienteId);

        // 2. Obtenemos el paciente
        Paciente paciente = pacienteService.findPacienteById(pacienteId);

        // 3. Si no existe el historial, inicializamos uno vacío para evitar errores en
        // la vista
        if (historial == null) {
            historial = new HistorialClinico();
            historial.setPaciente(paciente);
            historial.setEpisodios(new java.util.ArrayList<>());
        }

        // 4. LÓGICA DE FILTRADO POR FECHAS
        if (fechaInicio != null || fechaFin != null) {
            java.util.List<EpisodioClinico> episodiosFiltrados = historial.getEpisodios().stream()
                    .filter(ep -> {
                        java.time.LocalDate fechaEp = ep.getFecha().toLocalDate();
                        boolean matches = true;
                        if (fechaInicio != null && fechaEp.isBefore(fechaInicio))
                            matches = false;
                        if (fechaFin != null && fechaEp.isAfter(fechaFin))
                            matches = false;
                        return matches;
                    })
                    .collect(java.util.stream.Collectors.toList());

            historial.setEpisodios(episodiosFiltrados);
        }

        // 5. Atributos del modelo
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("historial", historial);
        model.addAttribute("citaIdOrigen", citaId);
        model.addAttribute("paciente", paciente);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "agenda");

        return "Medico/historial";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable("id") Long id, Model model) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");
        Empleado medico = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", medico);
        model.addAttribute("medicoLogueado", medico);
        return "Empleado/detalle";
    }

    @GetMapping("/agenda")
    public String mostrarAgenda(Model model, Authentication auth,
            @RequestParam(name = "fecha", required = false) String fechaStr) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        LocalDate hoy = LocalDate.now();
        LocalDate fechaBusqueda;

        try {
            // Si no hay fecha, usamos hoy. Si hay, la parseamos.
            // HEMOS ELIMINADO el "if (fechaBusqueda.isBefore(hoy))"
            fechaBusqueda = (fechaStr == null || fechaStr.isEmpty()) ? hoy : LocalDate.parse(fechaStr);
        } catch (Exception e) {
            fechaBusqueda = hoy;
        }

        // El service ahora devolverá todas las citas del día (Realizadas, Ausentes,
        // etc.)
        // gracias al cambio que hicimos en el ServiceImpl usando NotEstado(ANULADA)
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("citas", citaService.obtenerCitasDia(medico.getId(), fechaBusqueda));
        model.addAttribute("fechaActual", fechaBusqueda);
        model.addAttribute("hoy", hoy);
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

        // Listas para los Selectores Filtrables
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

        // Obtenemos el historial (el service ya carga todo lo necesario/Lazy)
        HistorialClinico historial = consultaService.obtenerOSolucionarHistorial(cita.getPaciente().getId());

        // FILTRO PRECISO: Buscamos el episodio que coincida exactamente con la fecha y
        // hora de la cita
        // Usamos chronounit para ignorar milisegundos si fuera necesario
        EpisodioClinico episodio = historial.getEpisodios().stream()
                .filter(e -> e.getFecha().isEqual(cita.getFechaHora()))
                .findFirst()
                .orElse(null);

        // Si por algún motivo la hora exacta falla (por diferencia de segundos al
        // guardar),
        // usamos un margen de 1 minuto:
        if (episodio == null) {
            episodio = historial.getEpisodios().stream()
                    .filter(e -> e.getFecha().isAfter(cita.getFechaHora().minusMinutes(1))
                            && e.getFecha().isBefore(cita.getFechaHora().plusMinutes(1)))
                    .findFirst()
                    .orElse(null);
        }

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

        // 1. Preparar Episodio con el Diagnóstico seleccionado
        EpisodioClinico episodio = new EpisodioClinico();
        Diagnostico diag = diagnosticoService.buscarPorId(diagnosticoId)
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con ID: " + diagnosticoId));
        episodio.setDiagnostico(diag);
        episodio.setFecha(LocalDateTime.now());

        // 2. Preparar Receta/Medicamentos
        List<LineaReceta> lineas = new ArrayList<>();
        if (medIds != null) {
            for (int i = 0; i < medIds.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setMedicamento(medicamentoService.encontrarMedicamento(medIds.get(i)));
                lr.setDosis(medDosis.get(i));
                lineas.add(lr);
            }
        }

        // 3. Preparar Pruebas
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebaIds != null) {
            for (Long pId : pruebaIds) {
                pruebas.add(pruebaService.encontrarPrueba(pId));
            }
        }

        // 4. Configurar fechas de tratamiento
        tratamiento.setFechaInicio(LocalDate.now());
        // La fechaFin viene del formulario

        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);
        return "redirect:/medico/agenda?success=true";
    }

    @GetMapping("/cita/no-asistio/{id}")
    public String marcarNoAsistio(@PathVariable Long id, RedirectAttributes flash) {
        try {
            // Usamos el servicio para cambiar el estado a AUSENTE
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
        org.springframework.data.domain.Page<cat.copernic.proyecto2.Domain.Administración.Nomina> paginaNominas = nominaService
                .getNominasPorEmpleado(medico.getId(), org.springframework.data.domain.PageRequest.of(page, size));
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Nomina/listarNominas";
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        org.springframework.data.domain.Page<cat.copernic.proyecto2.Domain.Administración.Nomina> paginaNominas = nominaService
                .getNominasPorEmpleado(medico.getId(), org.springframework.data.domain.PageRequest.of(page, size));
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "Nomina/listarNominas :: tablaNominas";
    }

    @PostMapping("/atender/finalizar")
    public String finalizarConsulta(@RequestParam Long citaId,
            @ModelAttribute EpisodioClinico episodio,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(required = false) List<Long> pruebasIds,
            @RequestParam(required = false) List<String> medNombre,
            @RequestParam(required = false) List<String> medDosis,
            @RequestParam(required = false) List<Double> medPrecio) {
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebasIds != null) {
            pruebasIds.forEach(id -> pruebas.add(pruebaService.encontrarPrueba(id)));
        }
        List<LineaReceta> lineas = new ArrayList<>();
        if (medNombre != null) {
            for (int i = 0; i < medNombre.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setDosis(medDosis.get(i));
                lineas.add(lr);
            }
        }
        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);
        return "redirect:/medico/dashboard?success";
    }
}