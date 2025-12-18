package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    // Listar empleados
    @GetMapping("")
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.listarTodos());
        return "empleado/listar";
    }

    // Mostrar formulario para añadir empleado
    @GetMapping("/nuevo")
    public String mostrarFormularioAñadir(Model model) {
        model.addAttribute("empleado", new Empleado());
        return "empleado/añadir";
    }

    // Guardar nuevo empleado
    @PostMapping("/guardar")
    public String guardarEmpleado(@ModelAttribute Empleado empleado,Model model) {
        try{
            empleadoService.crear(empleado);
            return "redirect:/empleados";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return("empleado/añadir");
        }

    }

    // Ver detalle de un empleado
    @GetMapping("/detalle/{id}")
    public String detalleEmpleado(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        return "empleado/detalle";
    }


    // Mostrar formulario para modificar empleado
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(id));
        return "empleado/modificar";
    }

    // Actualizar empleado
    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado( @ModelAttribute Empleado empleado,Model model) {

        try{
            empleadoService.actualizar(empleado.getId(), empleado);
            return "redirect:/empleados";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return"empleado/modificar";
        }
    }

    // Eliminar empleado (opcional)
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return "redirect:/empleados";
    }
}
