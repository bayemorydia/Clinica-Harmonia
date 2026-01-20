package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService;
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

    @GetMapping("")
    public String verMisVacaciones(Model model, Principal principal) {
        Usuario usuario = obtenerUsuarioLogueado(principal);
        if (usuario == null || usuario.getEmpleado() == null) {
            return "redirect:/login";
        }
        Empleado empleado = usuario.getEmpleado();
        List<Vacaciones> solicitudes = vacacionesService.listarVacacionesPorEmpleado(empleado.getId());
        long diasDisponibles = vacacionesService.calcularDiasDisponibles(empleado);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("diasDisponibles", diasDisponibles);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");
        return "empleado/vacaciones";
    }

    @PostMapping("/solicitar")
    public String solicitarVacaciones(@RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obtenerUsuarioLogueado(principal);
        if (usuario == null || usuario.getEmpleado() == null)
            return "redirect:/login";
        Empleado empleado = usuario.getEmpleado();
        if (fechaInicio.isAfter(fechaFin)) {
            redirectAttributes.addAttribute("error", "fecha");
            return "redirect:/vacaciones";
        }
        if (vacacionesService.existeSolapamiento(empleado.getId(), fechaInicio, fechaFin)) {
            redirectAttributes.addAttribute("error", "solapamiento");
            return "redirect:/vacaciones";
        }
        Vacaciones vacacion = new Vacaciones();
        vacacion.setEmpleado(empleado);
        vacacion.setFechaInicio(fechaInicio);
        vacacion.setFechaFin(fechaFin);
        vacacion.setEstado(EstadoVacaciones.PENDIENTE);
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        vacacion.setTotalDias((int) dias);
        vacacionesService.guardarVacacion(vacacion);
        redirectAttributes.addAttribute("success", true);
        return "redirect:/vacaciones";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable("id") Long id) {
        vacacionesService.eliminarVacacion(id);
        return "redirect:/vacaciones";
    }

    private Usuario obtenerUsuarioLogueado(Principal principal) {
        if (principal == null)
            return null;
        var page = usuarioService.buscarPorUsername(principal.getName(), PageRequest.of(0, 1));
        return page.hasContent() ? page.getContent().get(0) : null;
    }
}