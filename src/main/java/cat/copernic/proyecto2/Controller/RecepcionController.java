package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/recepcion")
public class RecepcionController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("fechaHoy", hoy);
        List<Cita> citasHoy = citaService.listarCitasDeHoyGlobal();
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("totalCitasHoy", citasHoy.size());
        model.addAttribute("totalPacientes", pacienteService.getAllPacientes().size());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        return "Recepcion/dashboard";
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            var page = usuarioService.buscarPorUsername(username, PageRequest.of(0, 1));
            if (page.hasContent()) {
                Usuario usuario = page.getContent().get(0);
                if (usuario.getEmpleado() != null) {
                    return "redirect:/empleados/detalle?id=" + usuario.getEmpleado().getId();
                }
            }
        }
        return "redirect:/recepcion/dashboard";
    }

    @GetMapping("/nominas")
    public String verNominas(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            var page = usuarioService.buscarPorUsername(username, PageRequest.of(0, 1));
            if (page.hasContent()) {
                Usuario usuario = page.getContent().get(0);
                if (usuario.getEmpleado() != null) {
                    return "redirect:/empleados/empleado/" + usuario.getEmpleado().getId();
                }
            }
        }
        return "redirect:/recepcion/dashboard";
    }

    @GetMapping("/vacaciones")
    public String verVacaciones(Model model) {
        model.addAttribute("activePage", "vacaciones");
        model.addAttribute("trad", traduccionService);
        return "empleado/vacaciones";
    }
}