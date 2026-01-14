package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la gestión de Empleados.
 * Implementa inyección por constructor mediante Lombok.
 */
@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final RolService rolService;
    private final TraduccionService traduccionService;
    private final UsuarioService usuarioService;
    private final TurnoService turnoService;

    private void cargarListasSoporte(Model model) {
        model.addAttribute("roles", rolService.listarRoles(PageRequest.of(0, 200)).getContent());
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("trad", traduccionService);
    }

    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        return "empleado/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("empleado", new Empleado());
        cargarListasSoporte(model);
        return "empleado/añadir";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            cargarListasSoporte(model);
            return "empleado/añadir";
        }
        try {
            Usuario usuarioCreado = usuarioService.crearUsuarioPaciente(
                    empleado.getNombre(), empleado.getApellidos(), empleado.getDni());
            empleado.setUsuario(usuarioCreado);
            empleadoService.crear(empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            cargarListasSoporte(model);
            return "empleado/añadir";
        }
    }

    @GetMapping("/empleado/{empleadoId}")
    public String listarNominasEmpleado(
            @PathVariable Long empleadoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        Page<Nomina> nominasPage = empleadoService.listarNominasPorEmpleado(empleadoId, search, page, size);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("empleado", empleadoService.obtenerPorId(empleadoId));
        model.addAttribute("nominas", nominasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", nominasPage.getTotalPages());
        model.addAttribute("totalElements", nominasPage.getTotalElements());
        model.addAttribute("search", search);
        return "empleado/nomina";
    }

    @GetMapping("/editar")
    public String mostrarFormularioEditar(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        cargarListasSoporte(model);
        return "empleado/modificar";
    }

    @PostMapping("/actualizar")
    public String actualizarEmpleado(
            @RequestParam Long id,
            @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            cargarListasSoporte(model);
            return "empleado/detalle";
        }
        try {
            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarListasSoporte(model);
            return "empleado/detalle";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        cargarListasSoporte(model);
        return "empleado/detalle";
    }

    @GetMapping("/confirmarBaja/{id}")
    public String confirmarBaja(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "empleado/confirmarBajaEmpleado";
    }

    @PostMapping("/baja/{id}")
    public String bajaEmpleado(@PathVariable Long id) {
        empleadoService.desactivarEmpleado(id);
        return "redirect:/empleados";
    }

    @GetMapping("/activar/{id}")
    public String activarEmpleado(@PathVariable Long id) {
        Empleado emp = empleadoService.obtenerPorId(id);
        emp.setFecha_baja(null);
        emp.setActiva(true);
        empleadoService.actualizar(id, emp);
        return "redirect:/empleados";
    }
}