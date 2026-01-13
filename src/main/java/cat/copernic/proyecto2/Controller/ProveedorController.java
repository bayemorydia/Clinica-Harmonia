package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Proveedor.ProveedorService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Usuario.Proveedor.UsuarioProveedorService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private UsuarioProveedorService usuarioProveedorService; // ✅ Servicio específico para proveedores

    private void cargarDatosExtra(Model model) {
        // Para cargar datos extra si hace falta
    }

    @GetMapping("")
    public String listarProveedores(Model model) {
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
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
            return "proveedor/añadir";
        }

        try {
            // 1️⃣ Crear usuario primero
            String nombre = proveedor.getNombre().trim();
            String cif = proveedor.getCif().trim();

            var usuario = usuarioProveedorService.crearUsuarioProveedor(nombre, cif);

            // 2️⃣ Asociar usuario al proveedor
            proveedor.setUsuario(usuario);

            // 3️⃣ Guardar proveedor con usuario
            proveedorService.crearProveedor(proveedor);

            return "redirect:/proveedores";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "proveedor/añadir";
        }
    }


    @GetMapping("/editar")
    public String mostrarFormularioEditar(@RequestParam("id") Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorID(id));
        model.addAttribute("trad", traduccionService);
        cargarDatosExtra(model);
        return "proveedor/detalle";
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
            return "proveedor/detalle";
        }

        try {
            proveedorService.actualizarProveedor(id, proveedor);
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            cargarDatosExtra(model);
            return "proveedor/detalle";
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
}
