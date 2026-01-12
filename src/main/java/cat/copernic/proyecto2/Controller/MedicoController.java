package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");

        // 1. Obtener el médico logueado
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("medicoLogueado", medico);

        // 2. Rango de HOY
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);

        // 3. ESTADÍSTICA: Pacientes con citas pendientes (no antiguos)
        model.addAttribute("totalPacientes", citaService.countPacientesPendientesPorMedico(medico.getId()));

        // 4. ESTADÍSTICA: Citas asignadas hoy
        model.addAttribute("citasHoy", citaService.countCitasHoyPorMedico(medico.getId(), inicioHoy, finHoy));

        // 5. LISTA: Citas pendientes (solo las asignadas a él que faltan por atender)
        model.addAttribute("citasPendientes", citaService.obtenerCitasPendientesMedico(medico.getId()));

        return "Medico/dashboard";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable("id") Long id, Model model) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");

        Empleado medico = empleadoService.obtenerPorId(id);

        // CAMBIO: Usa "empleado" en lugar de "medicoLogueado"
        // para que coincida con tu detalle.html
        model.addAttribute("empleado", medico);

        // También enviamos medicoLogueado por si el layout lo necesita para el sidebar
        model.addAttribute("medicoLogueado", medico);

        return "Empleado/detalle";
    }

    @GetMapping("/agenda")
public String mostrarAgenda(Model model, Authentication auth,
                            @RequestParam(name = "fecha", required = false) String fechaStr) {
    
    Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
    LocalDate hoy = LocalDate.now();
    LocalDate fechaBusqueda;

    // 1. Validar y parsear la fecha recibida
    try {
        fechaBusqueda = (fechaStr == null) ? hoy : LocalDate.parse(fechaStr);
        // 2. Bloquear fechas pasadas: si es anterior a hoy, forzamos a hoy
        if (fechaBusqueda.isBefore(hoy)) {
            fechaBusqueda = hoy;
        }
    } catch (Exception e) {
        fechaBusqueda = hoy;
    }

    // 3. Obtener citas del día seleccionado
    List<Cita> listaCitas = citaService.obtenerCitasDia(medico.getId(), fechaBusqueda);
    
    model.addAttribute("medicoLogueado", medico);
    model.addAttribute("citas", listaCitas);
    model.addAttribute("fechaActual", fechaBusqueda); // Para mostrar en el calendario
    model.addAttribute("hoy", hoy); // Para el botón "Hoy"
    model.addAttribute("activePage", "agenda");
    model.addAttribute("trad", traduccionService);

    return "Medico/agenda";
}

    @GetMapping("/agenda/dia/{fecha}")
    public String detalleDia(@PathVariable String fecha, Model model, Authentication auth) {
        LocalDate date = LocalDate.parse(fecha);
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("citas", citaService.obtenerCitasDia(medico.getId(), date));
        model.addAttribute("fechaSeleccionada", date);
        model.addAttribute("trad", traduccionService);
        return "Medico/agenda_detalle_dia :: listaCitas"; // Retorno de fragmento para modal o zona
    }
}