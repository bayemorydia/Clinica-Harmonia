package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import cat.copernic.proyecto2.Service.Prueba.PruebaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pruebas")
public class PruebaController {

    @Autowired
    private PruebaService pruebaService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping({ "", "/listar" })
    public String listar(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<PruebaMedica> pagePruebas = pruebaService.getPruebasPaginadas(query, activo, page, size);
        model.addAttribute("pruebas", pagePruebas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagePruebas.getTotalPages());
        model.addAttribute("totalElements", pagePruebas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Prueba/listaPruebas";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<PruebaMedica> pagePruebas = pruebaService.getPruebasPaginadas(query, activo, page, size);
        model.addAttribute("pruebas", pagePruebas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagePruebas.getTotalPages());
        model.addAttribute("totalElements", pagePruebas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Prueba/listaPruebas :: tablaPruebas";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        PruebaMedica prueba = new PruebaMedica();
        prueba.setPrecio(new BigDecimal("0"));
        model.addAttribute("pruebaMedica", prueba);
        model.addAttribute("trad", traduccionService);
        return "Prueba/formPrueba";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("pruebaMedica") PruebaMedica prueba,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "Prueba/formPrueba";
        }
        try {
            pruebaService.guardar(prueba);
            flash.addFlashAttribute("success", "Operación realizada con éxito.");
            return "redirect:/pruebas/listar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trad", traduccionService);
            return "Prueba/formPrueba";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        PruebaMedica prueba = pruebaService.encontrarPrueba(id);
        model.addAttribute("pruebaMedica", prueba);
        model.addAttribute("trad", traduccionService);
        return "Prueba/formPrueba";
    }

    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable("id") Long id, Model model) {
        PruebaMedica prueba = pruebaService.encontrarPrueba(id);
        model.addAttribute("prueba", prueba);
        model.addAttribute("trad", traduccionService);
        return "Prueba/confirmarDesactivarPrueba";
    }

    @PostMapping("/desactivar")
    public String desactivar(@RequestParam("id") Long id, RedirectAttributes flash) {
        pruebaService.eliminarLogica(id);
        flash.addFlashAttribute("warning", "Prueba desactivada correctamente.");
        return "redirect:/pruebas/listar";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable("id") Long id, RedirectAttributes flash) {
        pruebaService.activarLogica(id);
        flash.addFlashAttribute("success", "Prueba reactivada.");
        return "redirect:/pruebas/listar";
    }

    @GetMapping("")
    public String index() {
        return "redirect:/pruebas/listar";
    }
}