package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
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

import java.util.List;

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

    // ==========================
    // DASHBOARD PRINCIPAL RRHH
    // ==========================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        long totalEmpleados = empleadoService.listarTodos().size();
        long empleadosActivos = empleadoService.listarTodos().stream().filter(Empleado::getActiva).count();
        long empleadosInactivos = totalEmpleados - empleadosActivos;
        long nominasPendientes = 0; // placeholder
        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());

        model.addAttribute("empleadoLogueado", empleado);
        model.addAttribute("totalEmpleados", totalEmpleados);
        model.addAttribute("empleadosActivos", empleadosActivos);
        model.addAttribute("empleadosInactivos", empleadosInactivos);
        model.addAttribute("nominasPendientes", nominasPendientes);
        model.addAttribute("trad", traduccionService);

        return "RRHH/dashboard";
    }

    // ==========================
    // LISTADO COMPLETO DE EMPLEADOS
    // ==========================
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

    // ==========================
    // GESTIÓN DE NÓMINAS
    // ==========================
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

    // ==========================
    // PERFIL DE EMPLEADO
    // ==========================
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

    // ==========================
    // LISTADO DE VACACIONES (RRHH)
    // ==========================
    @GetMapping("/vacaciones")
    public String listarVacaciones(Model model, Authentication auth) {
        List<Vacaciones> solicitudes = vacacionesService.listarTodasLasVacaciones();

        long pendientes = vacacionesService.contarPendientes();

        model.addAttribute("pendientes", pendientes);

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "vacaciones");

        Empleado empleado = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("empleadoLogueado", empleado);

        return "RRHH/vacaciones"; // tu HTML de gestión de vacaciones
    }


}
