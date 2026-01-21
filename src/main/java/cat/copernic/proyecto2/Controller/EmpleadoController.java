package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService; // Inyectado el unificado
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Domain.Administración.Nomina;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private UsuarioService usuarioService; // Usando el servicio unificado

    @Autowired
    private NominaService nominaService;

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
            Rol rol = rolService.encontrarRol(empleado.getRol().getId());
            empleado.setRol(rol);

            // USANDO EL SERVICIO UNIFICADO (Contraseña automática por DNI/NIE)
            Usuario usuarioCreado = usuarioService.crearUsuarioEmpleado(
                    empleado.getNombre(),
                    empleado.getApellidos(),
                    empleado.getDni(),
                    rol.getNombre());

            empleado.setUsuario(usuarioCreado);
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
        if (result.hasErrors()) {
            cargarListasSoporte(model);
            return "Empleado/detalle";
        }
        try {
            Empleado empleadoExistente = empleadoService.obtenerPorId(id);
            Rol rolNuevo = rolService.encontrarRol(empleado.getRol().getId());

            // USANDO EL SERVICIO UNIFICADO PARA ACTUALIZAR ROL DE SEGURIDAD
            if (empleadoExistente.getUsuario() != null) {
                usuarioService.actualizarRolUsuario(empleadoExistente.getUsuario().getId(),
                        rolNuevo.getNombre());
            }

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

    @GetMapping("/activar/{id}")
    public String activarEmpleado(@PathVariable("id") Long id) {
        empleadoService.activarEmpleado(id);
        return "redirect:/empleados";
    }

    @GetMapping("/nominas/{id}")
    public String verNominasEmpleado(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Empleado empleado = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", empleado);

        Pageable pageable = PageRequest.of(page, size);
        Page<Nomina> nominaPage = nominaService.getNominasPorEmpleado(id, pageable);

        model.addAttribute("nominas", nominaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", nominaPage.getTotalPages());
        model.addAttribute("totalElements", nominaPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);

        return "Empleado/nominasEmpleado";
    }
}