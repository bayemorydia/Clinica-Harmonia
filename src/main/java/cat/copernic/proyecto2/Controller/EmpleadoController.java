package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cat.copernic.proyecto2.Domain.Persona.Usuario;

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
    private UsuarioService usuarioService; // <-- Inyectamos tu UsuarioService

    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    @GetMapping("")
    public String listarEmpleados(Model model) {
        int currentPage = 0; // Página inicial
        int size = 10;       // Tamaño de página
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("size", size);
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        return "empleado/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);
        return "empleado/añadir";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(
            @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result,
            Model model) {

        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            cargarRoles(model);
            return "empleado/añadir";
        }

        try {
            // 1️⃣ Crear empleado
            empleadoService.crear(empleado);

            // 2️⃣ Crear usuario automáticamente
            Usuario usuario = usuarioService.crearUsuarioPaciente(
                    empleado.getNombre(),
                    empleado.getApellidos(),
                    empleado.getDni()
            );

            // 3️⃣ Asignar usuario al empleado y actualizar
            empleado.setUsuario(usuario);
            empleadoService.actualizar(empleado.getId(), empleado);

            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            return "empleado/añadir";
        }
    }


    @GetMapping("/editar")
    public String mostrarFormularioEditar(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);
        return "empleado/modificar";
    }

    @PostMapping("/actualizar")
    public String actualizarEmpleado(
            @RequestParam Long id,
            @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result,
            Model model) {

        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            cargarRoles(model);
            return "empleado/detalle";
        }

        try {
            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            return "empleado/detalle";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);

        return "empleado/detalle";
    }

    // =======================================
    // BAJA Y ACTIVACIÓN CON PATH VARIABLE
    // =======================================

    @GetMapping("/confirmarBaja/{id}")
    public String confirmarBaja(@PathVariable("id") Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "empleado/confirmarBajaEmpleado";
    }

    @PostMapping("/baja/{id}")
    public String bajaEmpleado(@PathVariable("id") Long id) {
        empleadoService.desactivarEmpleado(id);
        return "redirect:/empleados";
    }

    @GetMapping("/activar/{id}")
    public String activarEmpleado(@PathVariable("id") Long id) {
        Empleado emp = empleadoService.obtenerPorId(id);
        emp.setFecha_baja(null);
        emp.setActiva(true);
        empleadoService.actualizar(id, emp);
        return "redirect:/empleados";
    }


}
