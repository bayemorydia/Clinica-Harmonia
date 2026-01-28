package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Vacaciones.RRHH.VacacionesServiceRRHH;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/RRHH")
public class RRHHController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private NominaService nominaService;

    @Autowired
    private VacacionesServiceRRHH vacacionesService;

    private void cargarTurnos(Model model) {
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Map<String, Long> estadisticas = empleadoService.obtenerEstadisticasDashboard();
        Empleado empleado = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalEmpleados", estadisticas.get("total"));
        model.addAttribute("empleadosActivos", estadisticas.get("activos"));
        model.addAttribute("empleadosInactivos", estadisticas.get("inactivos"));
        model.addAttribute("trad", traduccionService);
        return "RRHH/dashboard";
    }

    @GetMapping("/empleados")
    public String listarEmpleados(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model, Principal principal) {
        Page<Empleado> empleadoPage = empleadoService.getEmpleadosPaginados(query, activo, page, size);
        Empleado empleado = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("empleados", empleadoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", empleadoPage.getTotalPages());
        model.addAttribute("totalElements", empleadoPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("activePage", "empleados");
        return "Empleado/listar";
    }

    @GetMapping("/empleados/search")
    public String search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model, Principal principal) {
        Page<Empleado> empleadoPage = empleadoService.getEmpleadosPaginados(query, activo, page, size);
        Empleado empleado = empleadoService.obtenerPorUsername(principal.getName());
        model.addAttribute("empleados", empleadoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", empleadoPage.getTotalPages());
        model.addAttribute("totalElements", empleadoPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("activePage", "empleados");
        return "Empleado/listar :: tablaEmpleados";
    }

    @GetMapping("/nominas")
    public String listarNominas(Model model, Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {
        Empleado empleadoLogueado = empleadoService.obtenerPorUsername(principal.getName());
        Page<Nomina> paginaNominas = nominaService.listarTodasLasNominasPaginadas(query, page, size);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("empleadoLogueado", empleadoLogueado);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "nominas");
        return "Nomina/listarNominas";
    }

    @GetMapping("/nominas/search")
    public String searchNominasGlobal(Model model, Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String query) {
        Page<Nomina> paginaNominas = nominaService.listarTodasLasNominasPaginadas(query, page, size);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        return "Nomina/listarNominas :: tablaNominas";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable("id") Long id, Model model, Authentication auth) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");

        // 1. Obtenemos al usuario que está navegando (Tú)
        Empleado emplead2 = empleadoService.obtenerPorUsername(auth.getName());

        // 2. IMPORTANTE: Inyectamos ambos nombres para que cualquier layout lo
        // reconozca
        model.addAttribute("empleadoLogueado", emplead2); // Lo que espera layoutRRHH
        model.addAttribute("medicoLogueado", emplead2); // Lo que espera layoutMedico

        // 3. Cargamos los datos de soporte (roles, turnos)
        model.addAttribute("roles", rolService.RolesConcretos());
        cargarTurnos(model);

        // 4. Obtenemos al empleado que queremos visualizar (el del ID de la URL)
        Empleado empleado = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", empleado);

        return "Empleado/detalle";
    }

    @GetMapping("/gestionvacaciones")
    public String listarVacaciones(Model model, Authentication auth) {
        // La lógica ya estaba en el servicio, solo la invocamos
        model.addAttribute("pendientes", vacacionesService.contarPendientes());
        model.addAttribute("solicitudes", vacacionesService.listarTodasLasVacaciones());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "GestVacaciones");

        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", empleado);
        return "RRHH/vacaciones";
    }
}
