package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;

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
        Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(principal.getName());
        if (empleado == null) {
            return "redirect:/login?error=no_empleado";
        }
        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("solicitudes", vacacionesService.listarVacacionesPorEmpleado(empleado.getId()));
        model.addAttribute("diasDisponibles", vacacionesService.calcularDiasDisponibles(empleado));
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "misVacaciones");
        return "empleado/vacaciones";
    }

    @PostMapping("/solicitar")
    public String solicitarVacaciones(@RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            vacacionesService.procesarSolicitud(principal.getName(), fechaInicio, fechaFin);
            redirectAttributes.addFlashAttribute("success", "Solicitud enviada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vacaciones";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable("id") Long id) {
        vacacionesService.eliminarVacacion(id);
        return "redirect:/vacaciones";
    }
}