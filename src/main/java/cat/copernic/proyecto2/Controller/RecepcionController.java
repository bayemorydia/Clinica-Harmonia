package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private EmpleadoService empleadoService;

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
    public String listarMisNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Empleado recep = empleadoService.obtenerPorUsername(auth.getName());
        Page<Nomina> paginaNominas = nominaService.getNominasPorEmpleado(recep.getId(), PageRequest.of(page, size));

        model.addAttribute("empleado", recep); // <--- IMPORTANTE: Usar "empleado" para que el HTML lo detecte
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Empleado/nominasEmpleado"; // Tu vista específica
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {

        Empleado recep = empleadoService.obtenerPorUsername(auth.getName());

        // Usamos el nuevo método del service que filtra por empleado y periodo
        // (mes/año)
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(recep.getId(), query, page, size);

        model.addAttribute("empleado", recep);
        model.addAttribute("recepLogueado", recep);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query); // Mantener el valor en el input
        model.addAttribute("trad", traduccionService);

        return "Empleado/nominasEmpleado :: tablaNominas";
    }

}