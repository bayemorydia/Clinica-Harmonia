package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import cat.copernic.proyecto2.Service.Proveedor.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // LISTAR PROVEEDORES
    @GetMapping("")
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "listarProveedores"; // HTML Thymeleaf
    }

    // FORMULARIO AÑADIR PROVEEDOR
    @GetMapping("/nuevo")
    public String mostrarFormularioAñadir(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "añadirProveedor"; // HTML Thymeleaf
    }

    // GUARDAR NUEVO PROVEEDOR
    @PostMapping("/guardar")
    public String guardarProveedor(@ModelAttribute Proveedor proveedor, Model model) {
        try {
            proveedorService.crearProveedor(proveedor);
            return "redirect:/proveedores"; // si todo va bien
        } catch (IllegalArgumentException e) {
            // Capturamos solo el mensaje de la excepción
            model.addAttribute("error", e.getMessage());
            // Volvemos al formulario de nuevo con el mensaje
            return "añadirProveedor";
        }
    }

    // FORMULARIO MODIFICAR PROVEEDOR
    @GetMapping("/editar/{id}")
    public String mostrarFormularioModificar(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.buscarPorID(id);
        model.addAttribute("proveedor", proveedor);
        return "modificarProveedor"; // HTML Thymeleaf
    }

    // ACTUALIZAR PROVEEDOR
    @PostMapping("/actualizar")
    public String actualizarProveedor(@ModelAttribute Proveedor proveedor) {
        proveedorService.actualizarProveedor(proveedor.getId(), proveedor);
        return "redirect:/proveedores";
    }

    // VER DETALLES DEL PROVEEDOR
    @GetMapping("/{id}")
    public String verProveedor(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.buscarPorID(id);
        model.addAttribute("proveedor", proveedor);
        return "detalleProveedor"; // HTML Thymeleaf
    }

    // ELIMINAR PROVEEDOR
    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.buscarPorID(id);
        proveedorService.EliminarProveedor(id);
        return "redirect:/proveedores";
    }
}
