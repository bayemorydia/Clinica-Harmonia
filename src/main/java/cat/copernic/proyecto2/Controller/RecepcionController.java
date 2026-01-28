package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    private NominaService nominaService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TurnoService turnoService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("fechaHoy", LocalDate.now());
        List<Cita> citasHoy = citaService.listarCitasDeHoyGlobal();
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("totalCitasHoy", citasHoy.size());
        model.addAttribute("totalPacientes", pacienteService.obtenerTotalPacientes());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        return "Recepcion/dashboard";
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal, Model model) {
        Empleado empleado = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("empleado", empleado);
        model.addAttribute("roles", rolService.listarTodos());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");
        return "Empleado/detalle";
    }

    @GetMapping("/nominas")
    public String listarMisNominas(Model model, Principal principal, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String query) {
        Empleado recep = empleadoService.obtenerPorUsername(principal.getName());
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(recep.getId(), query, page, size);
        model.addAttribute("empleado", recep);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Empleado/nominasEmpleado";
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {
        Empleado recep = empleadoService.obtenerPorUsername(principal.getName());
        Page<Nomina> paginaNominas = nominaService.listarNominasPorEmpleado(recep.getId(), query, page, size);
        model.addAttribute("empleado", recep);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        return "Empleado/nominasEmpleado :: tablaNominas";
    }
}