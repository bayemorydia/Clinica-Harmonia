package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import cat.copernic.proyecto2.Service.Proveedor.ProveedorService;
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
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private TraduccionService traduccionService;

    // Método privado para futuras cargas de datos extra si hace falta
    private void cargarDatosExtra(Model model) {
        // Por ejemplo: cargar tipos de proveedor o categorías si existieran
        // model.addAttribute("categorias", categoriaService.listarTodas());
    }

    @GetMapping("")
    public String listarProveedores(Model model) {
        int currentPage = 0;
        int size = 10;
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("size", size);
        model.addAttribute("proveedores", proveedorService.listarTodos());
        model.addAttribute("trad", traduccionService);
        return "proveedor/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAnadir(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("trad", traduccionService);
        cargarDatosExtra(model);
        return "proveedor/añadir";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(
            @Valid @ModelAttribute("proveedor") Proveedor proveedor,
            BindingResult result,
            Model model) {

        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            cargarDatosExtra(model);
            return "proveedor/añadir";
        }

        try {
            proveedorService.crearProveedor(proveedor);
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarDatosExtra(model);
            return "proveedor/añadir";
        }
    }

    @GetMapping("/editar")
    public String mostrarFormularioEditar(@RequestParam("id") Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        cargarDatosExtra(model);
        return "proveedor/modificar";
    }

    @PostMapping("/actualizar")
    public String actualizarProveedor(
            @RequestParam("id") Long id,
            @Valid @ModelAttribute("proveedor") Proveedor proveedor,
            BindingResult result,
            Model model) {

        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            cargarDatosExtra(model);
            return "proveedor/modificar";
        }

        try {
            proveedorService.actualizarProveedor(id, proveedor);
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarDatosExtra(model);
            return "proveedor/modificar";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam("id") Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        return "proveedor/detalle";
    }

    @GetMapping("/confirmarBaja")
    public String confirmarBaja(@RequestParam("id") Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        return "proveedor/confirmarBajaProveedor";
    }

    @PostMapping("/baja")
    public String bajaProveedor(@RequestParam("id") Long id) {
        proveedorService.DesactivarProveedor(id);
        return "redirect:/proveedores";
    }

    @GetMapping("/activar")
    public String activarProveedor(@RequestParam("id") Long id) {
        proveedorService.activarProveedor(id);
        return "redirect:/proveedores";
    }

    @GetMapping("/confirmarEliminar")
    public String confirmarEliminar(@RequestParam("id") Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        return "proveedor/confirmarEliminarProveedor";
    }

    @PostMapping("/eliminarFisico")
    public String eliminarFisico(@RequestParam("id") Long id) {
        proveedorService.EliminarProveedor(id);
        return "redirect:/proveedores";
    }
}
