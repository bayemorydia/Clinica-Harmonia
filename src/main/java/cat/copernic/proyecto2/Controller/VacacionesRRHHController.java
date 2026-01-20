package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Service.Vacaciones.RRHH.VacacionesServiceRRHH;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/RRHH/vacaciones/gestion")
public class VacacionesRRHHController {

    @Autowired
    private VacacionesServiceRRHH vacacionesService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping
    public String mostrarSolicitudes(Model model) {
        List<Vacaciones> solicitudes = vacacionesService.listarTodasLasVacaciones();
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");
        return "RRHH/vacaciones";
    }

    @PostMapping("/aceptar/{id}")
    public String aceptarSolicitud(@PathVariable("id") Long id) {
        Vacaciones solicitud = vacacionesService.obtenerPorId(id);
        if (solicitud != null && solicitud.getEstado() == EstadoVacaciones.PENDIENTE) {
            vacacionesService.aprobarVacaciones(id);
        }
        return "redirect:/RRHH/vacaciones/gestion";
    }

    @PostMapping("/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable("id") Long id) {
        Vacaciones solicitud = vacacionesService.obtenerPorId(id);
        if (solicitud != null && solicitud.getEstado() == EstadoVacaciones.PENDIENTE) {
            vacacionesService.denegarVacaciones(id);
        }
        return "redirect:/RRHH/vacaciones/gestion";
    }
}
