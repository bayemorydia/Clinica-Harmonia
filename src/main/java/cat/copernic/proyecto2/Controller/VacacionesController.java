package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
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
        // Lógica delegada al service para obtener el empleado del principal
        Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(principal != null ? principal.getName() : null);
        
        if (empleado == null) {
            return "redirect:/login";
        }

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

        try {
            // Toda la validación y creación ocurre en el Service
            vacacionesService.procesarSolicitud(principal != null ? principal.getName() : null, fechaInicio, fechaFin);
            redirectAttributes.addAttribute("success", true);
        } catch (Exception e) {
            // El service lanza excepciones con el código de error ("fecha" o "solapamiento")
            redirectAttributes.addAttribute("error", e.getMessage());
            if ("login".equals(e.getMessage())) return "redirect:/login";
        }

        return "redirect:/vacaciones";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable("id") Long id) {
        vacacionesService.eliminarVacacion(id);
        return "redirect:/vacaciones";
    }
}