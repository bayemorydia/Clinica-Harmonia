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
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import cat.copernic.proyecto2.Domain.Administración.Turno;
import cat.copernic.proyecto2.Service.Empleado.Turno.TurnoService;
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


    @Autowired
    private TurnoService turnoService; // Nuevo servicio para turnos


    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    private void cargarTurnos(Model model) {
        turnoService.CrearTurnos();
        model.addAttribute("turnos", turnoService.listarTodos()); // ahora lista objetos Turno
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
        cargarTurnos(model);
        return "empleado/añadir";
    }



    @PostMapping("/guardar")
    public String guardarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado,
                                  BindingResult result, Model model) {
        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            System.out.println("error aqui");
            cargarRoles(model);
            cargarTurnos(model);
            return "empleado/añadir";
        }

        try {
            // 1. Crear el usuario (esto suele devolver el objeto Usuario guardado)
            Usuario usuarioCreado = usuarioService.crearUsuarioPaciente(
                    empleado.getNombre(), empleado.getApellidos(), empleado.getDni());

            // 2. Vincular el usuario al empleado ANTES de guardar el empleado
            empleado.setUsuario(usuarioCreado);

            // 3. Llamar a crear UNA SOLA VEZ
            empleadoService.crear(empleado);

            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
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

        Empleado empleado = empleadoService.obtenerPorId(empleadoId);
        Page<Nomina> nominasPage = empleadoService.listarNominasPorEmpleado(empleadoId, search, page, size);
        model.addAttribute("trad", traduccionService);

        model.addAttribute("empleado", empleado);
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
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);
        cargarTurnos(model);
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
            cargarTurnos(model);

            return "empleado/detalle";
        }

        try {
            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            cargarTurnos(model);

            return "empleado/detalle";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        cargarRoles(model);
        cargarTurnos(model);
        return "empleado/detalle";
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

      }catch (Exception e) {
            System.out.println("DEBUG: Error crítico al procesar la baja: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/empleados";
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
