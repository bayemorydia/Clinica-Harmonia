package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.empleado.EmpleadoUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import cat.copernic.proyecto2.Domain.Administración.Turno;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
import cat.copernic.proyecto2.Service.Validaciones.ValidacionesService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
    private EmpleadoUsuarioService EmpleadoUsuarioService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private ValidacionesService validacionesService;

    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    private void cargarTurnos(Model model) {
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos());
    }

    @GetMapping("")
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        return "empleado/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);
        cargarTurnos(model);
        return "empleado/añadir";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(
            @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        model.addAttribute("trad", traduccionService);

        // Validación manual de DNI/NIE
        try {
            validacionesService.validarDocumentoIdentidad(empleado.getDni());
        } catch (RuntimeException e) {
            result.rejectValue("dni", "dni.invalido", e.getMessage());
        }

        if (result.hasErrors()) {
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/añadir";
        }

        try {
            empleado.setUsuario(EmpleadoUsuarioService.crearEmpleadoConUsuario(
                    empleado.getNombre(),
                    empleado.getApellidos(),
                    empleado.getDni(),empleado.getRol().getNombre()));

            empleadoService.crear(empleado);
            redirectAttrs.addFlashAttribute("success", "Empleado guardado correctamente ✅");
            return "redirect:/empleados";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/añadir";

        } catch (RuntimeException e) {
            model.addAttribute("error", "❌ Error al guardar empleado: " + e.getMessage());
            cargarRoles(model);
            cargarTurnos(model);
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

        try {
            Empleado empleado = empleadoService.obtenerPorId(empleadoId);
            Page<Nomina> nominasPage = empleadoService.listarNominasPorEmpleado(empleadoId, search, page, size);

            model.addAttribute("empleado", empleado);
            model.addAttribute("nominas", nominasPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", nominasPage.getTotalPages());
            model.addAttribute("totalElements", nominasPage.getTotalElements());
            model.addAttribute("search", search);
            model.addAttribute("trad", traduccionService);

            return "empleado/nomina";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/empleados";
        }
    }

    @GetMapping("/editar")
    public String mostrarFormularioEditar(@RequestParam Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("empleado", empleadoService.obtenerPorId(id));
            model.addAttribute("trad", traduccionService);
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/modificar";
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleados";
        }
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
            cargarTurnos(model);
            return "empleado/detalle";
        }

        try {
            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/detalle";

        } catch (RuntimeException e) {
            model.addAttribute("error", "❌ Error al actualizar empleado: " + e.getMessage());
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/detalle";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        try {
            model.addAttribute("empleado", empleadoService.obtenerPorId(id));
            model.addAttribute("trad", traduccionService);
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/detalle";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/empleados";
        }
    }

    // =======================================
    // BAJA Y ACTIVACIÓN CON PATH VARIABLE
    // =======================================

    @GetMapping("/confirmarBaja/{id}")
    public String confirmarBaja(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("empleado", empleadoService.obtenerPorId(id));
            model.addAttribute("trad", traduccionService);
            return "empleado/confirmarBajaEmpleado";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/empleados";
        }
    }

    @PostMapping("/baja/{id}")
    public String bajaEmpleado(@PathVariable("id") Long id) {
        try {
            empleadoService.desactivarEmpleado(id);
        } catch (RuntimeException e) {
            return "redirect:/empleados?error=" + e.getMessage();
        }
        return "redirect:/empleados";
    }

    @GetMapping("/activar/{id}")
    public String activarEmpleado(@PathVariable("id") Long id) {
        try {
            Empleado emp = empleadoService.obtenerPorId(id);
            emp.setFecha_baja(null);
            emp.setActiva(true);
            empleadoService.actualizar(id, emp);
        } catch (RuntimeException e) {
            return "redirect:/empleados?error=" + e.getMessage();
        }
        return "redirect:/empleados";
    }
}
