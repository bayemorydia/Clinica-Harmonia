package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @Autowired
    private TraduccionService trad;

    /**
     * Lista principal de roles con paginación.
     */
    @GetMapping
    public String listar(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Rol> rolesPage = rolService.listarRoles(PageRequest.of(page, size));

        model.addAttribute("roles", rolesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rolesPage.getTotalPages());
        model.addAttribute("totalElements", rolesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", trad);

        return "Rol/listaRol";
    }

    /**
     * Endpoint para la búsqueda y filtrado dinámico (AJAX).
     * Retorna solo el fragmento de la tabla.
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Rol> rolesPage = (query == null || query.isEmpty())
                ? rolService.listarRoles(PageRequest.of(page, size))
                : rolService.buscarPorNombre(query, PageRequest.of(page, size));

        model.addAttribute("roles", rolesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rolesPage.getTotalPages());
        model.addAttribute("totalElements", rolesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", trad);

        return "Rol/listaRol :: tablaRoles";
    }

    /**
     * Muestra el formulario para crear un nuevo rol.
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Rol rol = new Rol();
        rol.setActivo(true); // Activo por defecto al crear
        model.addAttribute("rol", rol);
        model.addAttribute("trad", trad);
        return "Rol/formularioRol";
    }

    /**
     * Muestra el formulario para editar un rol existente.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        Rol rol = rolService.buscarRol(id);
        if (rol == null) {
            return "redirect:/admin/roles";
        }
        model.addAttribute("rol", rol);
        model.addAttribute("trad", trad);
        return "Rol/formularioRol";
    }

    /**
     * Guarda o actualiza un rol.
     * Incluye validación de duplicados mediante un bloque try-catch.
     */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Rol rol, Model model) {
        try {
            rolService.guardar(rol);
            return "redirect:/admin/roles";
        } catch (RuntimeException e) {
            // Si el servicio lanza una excepción (ej: nombre duplicado)
            model.addAttribute("rol", rol);
            model.addAttribute("trad", trad);
            // 'e.getMessage()' contiene la clave de traducción: "error.nombre.duplicado"
            model.addAttribute("error", trad.getTraduccion(e.getMessage()));
            return "Rol/formularioRol";
        }
    }

    /**
     * Activa un rol directamente (sin confirmación).
     */
    @GetMapping("/activar/{id}")
    public String activar(@PathVariable Long id) {
        Rol rol = rolService.buscarRol(id);
        if (rol != null) {
            rol.setActivo(true);
            rolService.guardar(rol);
        }
        return "redirect:/admin/roles";
    }

    /**
     * Navega a la pantalla de confirmación para desactivar.
     */
    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable Long id, Model model) {
        Rol rol = rolService.buscarRol(id);
        if (rol == null)
            return "redirect:/admin/roles";

        model.addAttribute("rol", rol);
        model.addAttribute("trad", trad);
        return "Rol/confirmarDesactivar";
    }

    /**
     * Procesa la desactivación efectiva tras la confirmación.
     */
    @PostMapping("/desactivar")
    public String desactivar(@RequestParam("id") Long id) {
        Rol rol = rolService.buscarRol(id);
        if (rol != null) {
            rol.setActivo(false);
            rolService.guardar(rol);
        }
        return "redirect:/admin/roles";
    }
}