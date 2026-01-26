package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
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

    @Autowired
    private NominaService nominaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("fechaHoy", LocalDate.now());
        List<Cita> citasHoy = citaService.listarCitasDeHoyGlobal();
        
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("totalCitasHoy", citasHoy.size());
        model.addAttribute("totalPacientes", pacienteService.obtenerTotalPacientes()); // Lógica movida al service
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        return "Recepcion/dashboard";
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal) {
        if (principal != null) {
            Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(principal.getName());
            if (empleado != null) {
                return "redirect:/empleados/detalle?id=" + empleado.getId();
            }
        }
        return "redirect:/recepcion/dashboard";
    }

    @GetMapping("/nominas")
    public String verNominas(Model model, Principal principal) {
        if (principal != null) {
            Empleado empleado = usuarioService.obtenerEmpleadoPorUsername(principal.getName());
            
            if (empleado != null) {
                // El service gestiona la carga de nóminas para evitar LazyInitializationException
                model.addAttribute("nominas", nominaService.getNominasPorEmpleado(empleado.getId(), PageRequest.of(0, 1000)).getContent());
                model.addAttribute("empleado", empleado);
                model.addAttribute("activePage", "nominas");
                model.addAttribute("trad", traduccionService);
                return "empleado/nominasEmpleado"; 
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