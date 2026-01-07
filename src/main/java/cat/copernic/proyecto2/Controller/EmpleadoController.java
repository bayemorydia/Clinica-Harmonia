package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TraduccionService traduccionService;

    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    @GetMapping("")
    public String listarEmpleados(Model model) {
        int currentPage = 0; // o el valor real de la página
        int size = 10;       // tamaño de página
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
            empleadoService.crear(empleado);
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
            return "empleado/modificar";
        }

        try {
            empleadoService.actualizar(id, empleado);
            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarRoles(model);
            return "empleado/modificar";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "empleado/detalle";
    }

    @GetMapping("/confirmarBaja")
    public String confirmarBaja(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "empleado/confirmarBajaEmpleado";
    }

    // Ejecutar BAJA (lógica)
    @PostMapping("/baja")
    public String bajaEmpleado(@RequestParam("id") Long id) {
        empleadoService.desactivarEmpleado(id);
        return "redirect:/empleados";
    }

    // Reactivar empleado
    @GetMapping("/activar")
    public String activarEmpleado(@RequestParam Long id) {
        Empleado emp = empleadoService.obtenerPorId(id);
        emp.setFecha_baja(null);
        emp.setActiva(true);
        empleadoService.actualizar(id, emp);
        return "redirect:/empleados";
    }

    // Confirmar ELIMINAR (físico)
    @GetMapping("/confirmarEliminar")
    public String confirmarEliminarFisico(@RequestParam Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        model.addAttribute("trad", traduccionService);
        return "empleado/confirmarEliminarEmpleado";
    }

    // Ejecutar ELIMINAR (físico)
    @PostMapping("/eliminarFisico")
    public String eliminarFisico(@RequestParam("id") Long id) {
        empleadoService.eliminarEmpleado(id);
        return "redirect:/empleados";
    }
}
