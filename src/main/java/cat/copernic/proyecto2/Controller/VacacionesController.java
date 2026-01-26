package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Vacaciones.VacacionesService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @GetMapping("") // Esto mapea a la URL: /vacaciones
    public String verMisVacaciones(Model model, Authentication auth) {
        if (auth == null)
            return "redirect:/login";

        Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(auth.getName());
        if (empleado == null)
            return "redirect:/login";

        // Inyectamos ambos nombres para que sea compatible con TODOS los layouts
        model.addAttribute("empleadoLogueado", empleado); // Para RRHH y Recepción
        model.addAttribute("medicoLogueado", empleado); // Para Médico
        model.addAttribute("recepLogueado", empleado); // Por si acaso para Recepción

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
            // Toda la validación y creación ocurre en el Service
            vacacionesService.procesarSolicitud(principal != null ? principal.getName() : null, fechaInicio, fechaFin);
            redirectAttributes.addAttribute("success", true);
        } catch (Exception e) {
            // El service lanza excepciones con el código de error ("fecha" o
            // "solapamiento")
            redirectAttributes.addAttribute("error", e.getMessage());
            if ("login".equals(e.getMessage()))
                return "redirect:/login";
        }

        return "redirect:/vacaciones";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable("id") Long id) {
        vacacionesService.eliminarVacacion(id);
        return "redirect:/vacaciones";
    }
}