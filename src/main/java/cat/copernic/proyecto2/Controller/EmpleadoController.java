package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
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
import cat.copernic.proyecto2.Service.Usuario.empleado.EmpleadoUsuarioService;
@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final RolService rolService;
    private final TraduccionService traduccionService;
    private final UsuarioService usuarioService;
    private final TurnoService turnoService;
    private final EmpleadoUsuarioService EmpleadoUsuarioService;

    private void cargarListasSoporte(Model model) {
        model.addAttribute("roles", rolService.RolesConcretos());
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("trad", traduccionService);
    }

    @GetMapping("")
    public String listar(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<Empleado> empleadoPage = empleadoService.getEmpleadosPaginados(query, activo, page, size);

        model.addAttribute("empleados", empleadoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", empleadoPage.getTotalPages());
        model.addAttribute("totalElements", empleadoPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);

        return "Empleado/listar";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<Empleado> empleadoPage = empleadoService.getEmpleadosPaginados(query, activo, page, size);

        model.addAttribute("empleados", empleadoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", empleadoPage.getTotalPages());
        model.addAttribute("totalElements", empleadoPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);

        return "Empleado/listar :: tablaEmpleados";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("empleado", new Empleado());
        cargarListasSoporte(model);
        return "Empleado/añadir";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            cargarListasSoporte(model);
            return "Empleado/añadir";
        }
        try {
            Usuario usuarioCreado = EmpleadoUsuarioService.crearEmpleadoConUsuario(
                    empleado.getNombre(),
                    empleado.getApellidos(),
                    empleado.getDni(),
                    empleado.getRol().getNombre()
            );
            empleadoService.crear(empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarListasSoporte(model);
            return "Empleado/añadir";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        cargarListasSoporte(model);
        return "Empleado/detalle";
    }

    @PostMapping("/actualizar")
    public String actualizarEmpleado(@RequestParam Long id, @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result, Model model) {


        Rol rol =rolService.encontrarRol(empleado.getRol().getId());

        Empleado empleadoUsuario= empleadoService.obtenerPorId(empleado.getId());

        if (result.hasErrors()) {
            cargarListasSoporte(model);
            return "Empleado/detalle";
        }
        try {

            EmpleadoUsuarioService.actualizarRolUsuario(empleadoUsuario.getUsuario().getId(), rol.getNombre());

            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarListasSoporte(model);
            return "Empleado/detalle";
        }
    }

    @GetMapping("/confirmarBaja/{id}")
    public String confirmarBaja(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "Empleado/confirmarBajaEmpleado";
    }

    @PostMapping("/baja/{id}")
    public String bajaEmpleado(@PathVariable Long id) {
        empleadoService.desactivarEmpleado(id);
        return "redirect:/empleados";
    }

    // CORRECCIÓN: Usamos PostMapping para activar y aseguramos la persistencia
    @PostMapping("/activar/{id}")
    public String activarEmpleado(@PathVariable Long id) {
        Empleado emp = empleadoService.obtenerPorId(id);
        emp.setFecha_baja(null); // Eliminamos la fecha de baja
        emp.setActiva(true); // Cambiamos el estado a true

        // Usamos el método guardar/actualizar del servicio.
        // Nota: Si el error persiste por validaciones de DNI,
        // asegúrate de que los datos en la BD sean válidos (ej: 9 dígitos en teléfono).
        empleadoService.actualizar(id, emp);

        return "redirect:/empleados";
    }
}
