package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import cat.copernic.proyecto2.Service.Proveedor.ProveedorService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.Proveedor.UsuarioProveedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired private ProveedorService proveedorService;
    @Autowired private TraduccionService traduccionService;
    @Autowired private UsuarioProveedorService usuarioProveedorService;

    @GetMapping("")
    public String listar(@RequestParam(defaultValue = "") String query,
                         @RequestParam(required = false) Boolean activo,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size, Model model) {
        
        Page<Proveedor> provPage = proveedorService.listarPaginado(query, activo, 
                PageRequest.of(page, size, Sort.by("nombre").ascending()));

        model.addAttribute("proveedores", provPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", provPage.getTotalPages());
        model.addAttribute("totalElements", provPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "proveedor/listar";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String query,
                         @RequestParam(required = false) Boolean activo,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size, Model model) {
        
        Page<Proveedor> provPage = proveedorService.listarPaginado(query, activo, 
                PageRequest.of(page, size, Sort.by("nombre").ascending()));

        model.addAttribute("proveedores", provPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", provPage.getTotalPages());
        model.addAttribute("totalElements", provPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "proveedor/listar :: tablaProveedores";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("trad", traduccionService);
        return "proveedor/añadir";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Proveedor proveedor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "proveedor/añadir";
        }
        try {
            var usuario = usuarioProveedorService.crearUsuarioProveedor(proveedor.getNombre(), proveedor.getCif());
            proveedor.setUsuario(usuario);
            proveedorService.crearProveedor(proveedor);
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trad", traduccionService);
            return "proveedor/añadir";
        }
    }

    @GetMapping("/detalle")
    public String detalle(@RequestParam Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        return "proveedor/añadir";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long id, @Valid @ModelAttribute Proveedor proveedor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "proveedor/detalle";
        }
        proveedorService.actualizarProveedor(id, proveedor);
        return "redirect:/proveedores";
    }

    @GetMapping("/confirmarBaja")
    public String confirmarBaja(@RequestParam Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        return "proveedor/confirmarBajaProveedor";
    }

    @PostMapping("/baja")
    public String baja(@RequestParam Long id) {
        proveedorService.desactivarProveedor(id);
        return "redirect:/proveedores";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id) {
        proveedorService.activarProveedor(id);
        return "redirect:/proveedores";
    }
}