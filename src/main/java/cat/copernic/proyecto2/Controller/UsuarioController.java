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

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TraduccionService trad;

    @GetMapping
    public String listar(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Usuario> usuariosPage = usuarioService.listarUsuarios(PageRequest.of(page, size));

        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", trad);

        return "Usuario/listaUsuario";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Usuario> usuariosPage = (query == null || query.isEmpty())
                ? usuarioService.listarUsuarios(PageRequest.of(page, size))
                : usuarioService.buscarPorUsername(query, PageRequest.of(page, size));

        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", trad);

        return "Usuario/listaUsuario :: tablaUsuarios";
    }

    @GetMapping("/activar/{id}")
    public String activar(@PathVariable Long id) {
        Usuario u = usuarioService.buscarUsuario(id);
        if (u != null) {
            u.setActivo(true);
            usuarioService.guardar(u); // Asegúrate que el método sea guardar o guardarUsuario
        }
        return "redirect:/admin/usuarios";
    }

    // 1. Mostrar la página de confirmación
    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable("id") Long id, Model model) {
        Usuario u = usuarioService.buscarUsuario(id); 
        
        if (u == null) {
            return "redirect:/admin/usuarios";
        }
        
        model.addAttribute("usuario", u);
        model.addAttribute("trad", trad);
        return "Usuario/confirmarDesactivar"; 
    }

    // 2. Procesar la desactivación (POST)
    @PostMapping("/desactivar")
    public String desactivarUsuario(@RequestParam("id") Long id) {
        Usuario u = usuarioService.buscarUsuario(id);
        if (u != null) {
            u.setActivo(false);
            usuarioService.guardar(u); 
        }
        return "redirect:/admin/usuarios";
    }
}