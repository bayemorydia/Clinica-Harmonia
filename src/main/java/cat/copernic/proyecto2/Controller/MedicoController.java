package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.PersonalSanitario.EpisodioClinico;
import cat.copernic.proyecto2.Domain.PersonalSanitario.LineaReceta;
import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import cat.copernic.proyecto2.Service.Prueba.PruebaService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Consulta.ConsultaService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private VacacionesService vacacionesService;

    @Autowired
    private NominaService nominaService;

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private PruebaService pruebaService;

    // --- DASHBOARD ---
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

    // --- GESTIÓN DE VACACIONES ---
    @GetMapping("/vacaciones")
    public String mostrarVacaciones(Model model, Authentication auth) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        LocalDate hoy = LocalDate.now();

        // El servicio se encarga del cálculo proporcional según fecha de alta
        long diasDisponibles = vacacionesService.calcularDiasDisponibles(medico);

        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("hoy", hoy);
        model.addAttribute("diasDisponibles", diasDisponibles);
        model.addAttribute("activePage", "vacaciones");
        model.addAttribute("trad", traduccionService);

        // Obtenemos las solicitudes reales de la base de datos SQL
        model.addAttribute("solicitudes", vacacionesService.listarVacacionesPorEmpleado(medico.getId()));

        return "Empleado/vacaciones";
    }

    @PostMapping("/vacaciones/solicitar")
    public String solicitarVacaciones(@RequestParam("fechaInicio") String inicio,
            @RequestParam("fechaFin") String fin,
            Authentication auth) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        LocalDate fechaIni = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);

        if (fechaFin.isBefore(fechaIni)) {
            return "redirect:/medico/vacaciones?error=fecha";
        }

        if (vacacionesService.existeSolapamiento(medico.getId(), fechaIni, fechaFin)) {
            return "redirect:/medico/vacaciones?error=solapamiento";
        }

        Vacaciones nueva = new Vacaciones();
        nueva.setEmpleado(medico);
        nueva.setFechaInicio(fechaIni);
        nueva.setFechaFin(fechaFin);
        nueva.setEstado(EstadoVacaciones.PENDIENTE);

        // Cálculo: se suma 1 porque si pides del 10 al 10, es 1 día de vacaciones
        long dias = ChronoUnit.DAYS.between(fechaIni, fechaFin) + 1;
        nueva.setTotalDias((int) dias);

        vacacionesService.guardarVacacion(nueva);

        return "redirect:/medico/vacaciones?success=true";
    }

    @PostMapping("/vacaciones/cancelar/{id}")
    public String cancelarVacaciones(@PathVariable("id") Long id) {
        // En una app profesional, podrías validar aquí que la vacación pertenezca al
        // médico
        vacacionesService.eliminarVacacion(id);
        return "redirect:/medico/vacaciones";
    }

    // --- PERFIL Y AGENDA ---
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
            fechaBusqueda = (fechaStr == null) ? hoy : LocalDate.parse(fechaStr);
            if (fechaBusqueda.isBefore(hoy))
                fechaBusqueda = hoy;
        } catch (Exception e) {
            fechaBusqueda = hoy;
        }

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
        // 1. Obtener el médico logueado (como haces en la agenda)
        // Usamos Authentication auth y el mismo servicio/método
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("listaMedicamentos", medicamentoService.listarMedicamentos());
        model.addAttribute("listaTratamientos", pruebaService.listarPruebas());

        // 2. Obtener la cita
        Cita cita = citaService.findCitaById(id);

        // 3. Pasar todos los objetos al modelo
        // Es vital que el nombre sea 'medicoLogueado' para el layoutMedico
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", cita.getPaciente());

        // Objetos vacíos para vincular con el formulario (th:object)
        model.addAttribute("episodio", new EpisodioClinico());
        model.addAttribute("tratamiento", new Tratamiento());

        // Datos de control de la interfaz
        model.addAttribute("activePage", "agenda");
        model.addAttribute("trad", traduccionService);

        return "Medico/atender";
    }

    @PostMapping("/finalizar-consulta")
    public String guardarConsulta(@RequestParam("citaId") Long citaId,
            @ModelAttribute EpisodioClinico episodio,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(value = "meds[]", required = false) List<String> meds,
            @RequestParam(value = "dosis[]", required = false) List<String> dosis,
            @RequestParam(value = "pruebaTipo[]", required = false) List<String> pruebaTipos,
            @RequestParam(value = "pruebaResultado[]", required = false) List<String> pruebaResultados) {

        // 1. Procesar Líneas de Receta
        List<LineaReceta> lineas = new ArrayList<>();
        if (meds != null) {
            for (int i = 0; i < meds.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setMedicamento(meds.get(i));
                lr.setDosis(dosis.get(i));
                lineas.add(lr);
            }
        }

        // 2. Procesar Pruebas Médicas
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebaTipos != null) {
            for (int i = 0; i < pruebaTipos.size(); i++) {
                if (!pruebaTipos.get(i).isBlank()) { // Solo si el tipo no está vacío
                    PruebaMedica pm = new PruebaMedica();
                    pm.setNombre(pruebaTipos.get(i));
                    pruebas.add(pm);
                }
            }
        }

        // 3. Llamada al servicio con el nuevo parámetro de pruebas
        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);

        return "redirect:/medico/agenda?success=true";
    }

    @GetMapping("/cita/estado/{id}/{estado}")
    public String cambiarEstado(@PathVariable Long id, @PathVariable String estado) {
        consultaService.cambiarEstadoCita(id, EstadoCita.valueOf(estado));
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
        model.addAttribute("query", ""); // Para que el input de búsqueda no de error

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

        // 1. Cargar objetos de Pruebas desde los IDs del catálogo
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebasIds != null) {
            pruebasIds.forEach(id -> pruebas.add(pruebaService.encontrarPrueba(id)));
        }

        // 2. Construir líneas de receta con el nuevo campo de precio
        List<LineaReceta> lineas = new ArrayList<>();
        if (medNombre != null) {
            for (int i = 0; i < medNombre.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setMedicamento(medNombre.get(i));
                lr.setDosis(medDosis.get(i));
                lr.setPrecioUnitario(medPrecio.get(i)); // Capturamos el precio unitario
                lineas.add(lr);
            }
        }

        // 3. Llamar al servicio corregido
        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);

        return "redirect:/medico/dashboard?success";
    }
}