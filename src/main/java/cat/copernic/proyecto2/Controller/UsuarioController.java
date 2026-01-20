package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TraduccionService trad;

    @GetMapping({ "", "/listar" })
    public String listar(Model model,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Usuario> usuariosPage = usuarioService.listarPaginado(query, activo, PageRequest.of(page, size));
        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", trad);
        return "Usuario/listaUsuario";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Usuario> usuariosPage = usuarioService.listarPaginado(query, activo, PageRequest.of(page, size));
        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", trad);
        return "Usuario/listaUsuario :: tablaUsuarios";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        Usuario u = usuarioService.buscarUsuario(id);
        if (u != null) {
            u.setActivo(true);
            usuarioService.guardar(u);
            flash.addFlashAttribute("success", "Usuario reactivado.");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable("id") Long id, Model model) {
        Usuario u = usuarioService.buscarUsuario(id);
        if (u == null)
            return "redirect:/admin/usuarios";
        model.addAttribute("usuario", u);
        model.addAttribute("trad", trad);
        return "Usuario/confirmarDesactivar";
    }

    @PostMapping("/desactivar")
    public String desactivarUsuario(@RequestParam("id") Long id, RedirectAttributes flash) {
        Usuario u = usuarioService.buscarUsuario(id);
        if (u != null) {
            u.setActivo(false);
            usuarioService.guardar(u);
            flash.addFlashAttribute("warning", "Usuario desactivado.");
        }
        return "redirect:/admin/usuarios";
    }
}