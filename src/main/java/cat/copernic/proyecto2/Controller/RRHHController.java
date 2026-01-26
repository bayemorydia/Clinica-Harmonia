package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Vacaciones.RRHH.VacacionesServiceRRHH;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    private void cargarTurnos(Model model) {
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        // Toda la lógica de conteo se ha movido al EmpleadoService
        Map<String, Long> estadisticas = empleadoService.obtenerEstadisticasDashboard();
        
        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", empleado);
        
        model.addAttribute("totalEmpleados", estadisticas.get("total"));
        model.addAttribute("empleadosActivos", estadisticas.get("activos"));
        model.addAttribute("empleadosInactivos", estadisticas.get("inactivos"));
        model.addAttribute("nominasPendientes", 0); // Mantengo el 0 que tenías
        
        model.addAttribute("trad", traduccionService);
        return "RRHH/dashboard";
    }

    @GetMapping("/empleados")
    public String listarEmpleados(Model model, Authentication auth) {
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        
        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("activePage", "empleados");
        return "empleado/listar";
    }

    @GetMapping("/nominas")
    public String listarNominas(Model model, Authentication auth) {
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        model.addAttribute("nominas", nominaService.getAllNominas());
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        
        Empleado emplead2 = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", emplead2);
        model.addAttribute("activePage", "nominas");
        return "Nomina/listarNominas";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable("id") Long id, Model model, Authentication auth) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");
        
        Empleado emplead2 = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", emplead2);
        
        cargarRoles(model);
        cargarTurnos(model);
        
        Empleado empleado = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", empleado);
        return "empleado/detalle";
    }

    @GetMapping("/vacaciones")
    public String listarVacaciones(Model model, Authentication auth) {
        // La lógica ya estaba en el servicio, solo la invocamos
        model.addAttribute("pendientes", vacacionesService.contarPendientes());
        model.addAttribute("solicitudes", vacacionesService.listarTodasLasVacaciones());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");
        
        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", empleado);
        return "RRHH/vacaciones";
    }
}