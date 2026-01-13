package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private CitaService citaService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private VacacionesService vacacionesService;

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
            @RequestParam(value = "comentario", required = false) String comentarioTxt,
            Authentication auth) {

        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        LocalDate fechaIni = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);

        // 1. Validación de orden de fechas
        if (fechaFin.isBefore(fechaIni)) {
            return "redirect:/medico/vacaciones?error=fecha";
        }

        // 2. NUEVA VALIDACIÓN: Solapamiento de días
        if (vacacionesService.existeSolapamiento(medico.getId(), fechaIni, fechaFin)) {
            return "redirect:/medico/vacaciones?error=solapamiento";
        }

        // Proceso de guardado normal
        Vacaciones nueva = new Vacaciones();
        nueva.setEmpleado(medico);
        nueva.setFechaInicio(fechaIni);
        nueva.setFechaFin(fechaFin);
        nueva.setEstado(EstadoVacaciones.PENDIENTE);

        int dias = (int) ChronoUnit.DAYS.between(fechaIni, fechaFin) + 1;
        nueva.setTotalDias(dias);

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
}