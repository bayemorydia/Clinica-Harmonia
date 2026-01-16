package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Vacaciones.RRHH.VacacionesServiceRRHH;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/RRHH/vacaciones/gestion") // <-- Subpath para evitar conflictos
public class VacacionesRRHHController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private VacacionesServiceRRHH vacacionesService;

    @Autowired
    private TraduccionService traduccionService;

    // --- MOSTRAR TODAS LAS SOLICITUDES ---
    @GetMapping
    public String mostrarSolicitudes(Model model) {
        List<Vacaciones> solicitudes = vacacionesService.listarTodasLasVacaciones();
        long pendientes = vacacionesService.contarPendientes();

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");

        return "RRHH/vacaciones"; // plantilla Thymeleaf
    }

    // --- ACEPTAR SOLICITUD ---
    @PostMapping("/aceptar/{id}")
    public String aceptarSolicitud(@PathVariable("id") Long id) {
        Vacaciones solicitud = vacacionesService.obtenerPorId(id);
        if (solicitud != null && solicitud.getEstado() == EstadoVacaciones.PENDIENTE) {
            vacacionesService.aprobarVacaciones(id);
        }
        return "redirect:/RRHH/vacaciones/gestion"; // redirige al subpath
    }

    // --- RECHAZAR SOLICITUD ---
    @PostMapping("/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable("id") Long id) {
        Vacaciones solicitud = vacacionesService.obtenerPorId(id);
        if (solicitud != null && solicitud.getEstado() == EstadoVacaciones.PENDIENTE) {
            vacacionesService.denegarVacaciones(id);
        }
        return "redirect:/RRHH/vacaciones/gestion"; // redirige al subpath
    }
}
