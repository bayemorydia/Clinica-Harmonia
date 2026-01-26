package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping({ "", "/listar" })
    public String listar(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Rol> rolesPage = rolService.getRolesPaginados(query, activo, page, size);
        model.addAttribute("roles", rolesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rolesPage.getTotalPages());
        model.addAttribute("totalElements", rolesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Rol/listaRol";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Rol> rolesPage = rolService.getRolesPaginados(query, activo, page, size);
        model.addAttribute("roles", rolesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rolesPage.getTotalPages());
        model.addAttribute("totalElements", rolesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Rol/listaRol :: tablaRoles";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        // Lógica de inicialización movida al service
        model.addAttribute("rol", rolService.prepararNuevoRol());
        model.addAttribute("trad", traduccionService);
        return "Rol/formularioRol";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("rol") Rol rol, BindingResult result, Model model,
            RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "Rol/formularioRol";
        }
        try {
            rolService.guardar(rol);
            flash.addFlashAttribute("success", "Operación realizada con éxito.");
            return "redirect:/roles/listar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trad", traduccionService);
            return "Rol/formularioRol";
        }
    }

    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.encontrarRol(id));
        model.addAttribute("trad", traduccionService);
        return "Rol/confirmarDesactivar";
    }

    @PostMapping("/desactivar")
    public String desactivar(@RequestParam Long id, RedirectAttributes flash) {
        rolService.desactivar(id);
        flash.addFlashAttribute("warning", "Rol desactivado.");
        return "redirect:/roles/listar";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        rolService.activar(id);
        flash.addFlashAttribute("success", "Rol activado.");
        return "redirect:/roles/listar";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.encontrarRol(id));
        model.addAttribute("trad", traduccionService);
        return "Rol/detalleRol";
    }
}