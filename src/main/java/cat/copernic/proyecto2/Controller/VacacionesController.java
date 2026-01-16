package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones; // Asegúrate de tener este Enum
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService; // Tu paquete correcto
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/vacaciones")
public class VacacionesController {

    @Autowired
    private VacacionesService vacacionesService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TraduccionService traduccionService;

    // 1. LISTAR VACACIONES
    @GetMapping("")
    public String verMisVacaciones(Model model, Principal principal) {
        
        Usuario usuario = obtenerUsuarioLogueado(principal);
        if (usuario == null || usuario.getEmpleado() == null) {
            return "redirect:/login";
        }

        Empleado empleado = usuario.getEmpleado();

        // ADAPTADO: Usamos 'listarVacacionesPorEmpleado'
        List<Vacaciones> solicitudes = vacacionesService.listarVacacionesPorEmpleado(empleado.getId());
        
        // ADAPTADO: Usamos 'calcularDiasDisponibles' pasando el OBJETO empleado
        long diasDisponibles = vacacionesService.calcularDiasDisponibles(empleado);

        // Pasamos datos a la vista
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("diasDisponibles", diasDisponibles);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");

        return "empleado/vacaciones";
    }

    // 2. SOLICITAR VACACIONES
    @PostMapping("/solicitar")
    public String solicitarVacaciones(@RequestParam("fechaInicio") LocalDate fechaInicio,
                                      @RequestParam("fechaFin") LocalDate fechaFin,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        
        Usuario usuario = obtenerUsuarioLogueado(principal);
        if (usuario == null || usuario.getEmpleado() == null) return "redirect:/login";

        Empleado empleado = usuario.getEmpleado();

        // VALIDACIÓN 1: Fechas coherentes
        if (fechaInicio.isAfter(fechaFin)) {
            redirectAttributes.addAttribute("error", "fecha");
            return "redirect:/vacaciones";
        }

        // VALIDACIÓN 2: Solapamiento (Usando tu servicio)
        if (vacacionesService.existeSolapamiento(empleado.getId(), fechaInicio, fechaFin)) {
            redirectAttributes.addAttribute("error", "solapamiento");
            return "redirect:/vacaciones";
        }

        // CREACIÓN DEL OBJETO (Lógica movida al controlador porque el servicio solo "guarda")
        Vacaciones vacacion = new Vacaciones();
        vacacion.setEmpleado(empleado);
        vacacion.setFechaInicio(fechaInicio);
        vacacion.setFechaFin(fechaFin);
        vacacion.setEstado(EstadoVacaciones.PENDIENTE); // Estado inicial
       
        
        // Calculamos los días totales (incluyendo el último día)
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        vacacion.setTotalDias((int) dias);

        // GUARDAR (Usando tu método 'guardarVacacion')
        vacacionesService.guardarVacacion(vacacion);
        
        redirectAttributes.addAttribute("success", true);
        return "redirect:/vacaciones";
    }

    // 3. CANCELAR SOLICITUD
    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable("id") Long id) {
        // ADAPTADO: Usamos 'eliminarVacacion'
        vacacionesService.eliminarVacacion(id);
        return "redirect:/vacaciones";
    }

    // Método auxiliar para obtener usuario
    private Usuario obtenerUsuarioLogueado(Principal principal) {
        if (principal == null) return null;
        // Usamos el método paginado que sabemos que SÍ existe en tu UsuarioService
        var page = usuarioService.buscarPorUsername(principal.getName(), PageRequest.of(0, 1));
        return page.hasContent() ? page.getContent().get(0) : null;
    }
}