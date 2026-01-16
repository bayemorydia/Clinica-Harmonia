package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Diagnostico;
import cat.copernic.proyecto2.Service.Diagnostico.DiagnosticoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/diagnosticos")
public class DiagnosticoController {

    @Autowired
    private DiagnosticoService diagnosticoService;

    @Autowired
    private TraduccionService trad;

    @GetMapping("")
    public String listar(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<Diagnostico> pagina = diagnosticoService.getDiagnosticosPaginados(keyword, activo, page, size);

        model.addAttribute("listaDiagnosticos", pagina.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagina.getTotalPages());
        model.addAttribute("totalItems", pagina.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", trad);

        return "Diagnostico/listaDiagnosticos";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        // Llamamos al método correcto del servicio que ya maneja la lógica de filtros y
        // paginación
        Page<Diagnostico> paginaDiagnosticos = diagnosticoService.getDiagnosticosPaginados(keyword, activo, page, size);

        model.addAttribute("listaDiagnosticos", paginaDiagnosticos.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaDiagnosticos.getTotalPages());
        model.addAttribute("totalItems", paginaDiagnosticos.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", trad);

        // Retornamos solo el fragmento de la tabla para la actualización AJAX
        return "Diagnostico/listaDiagnosticos :: tablaDiagnosticos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("diagnostico", new Diagnostico());
        model.addAttribute("trad", trad);
        return "Diagnostico/formDiagnostico";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Diagnostico diagnostico, RedirectAttributes attribute, Model model) {
        try {
            diagnosticoService.guardar(diagnostico);
            attribute.addFlashAttribute("success", "Diagnóstico guardado con éxito.");
            return "redirect:/diagnosticos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("diagnostico", diagnostico);
            model.addAttribute("trad", trad);
            return "Diagnostico/formDiagnostico";
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes attribute) {
        Optional<Diagnostico> diagnostico = diagnosticoService.buscarPorId(id);
        if (diagnostico.isPresent()) {
            model.addAttribute("diagnostico", diagnostico.get());
            model.addAttribute("trad", trad);
            return "Diagnostico/detalleDiagnostico";
        }
        attribute.addFlashAttribute("error", "Diagnóstico no encontrado.");
        return "redirect:/diagnosticos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes attribute) {
        Optional<Diagnostico> diagnostico = diagnosticoService.buscarPorId(id);
        if (diagnostico.isPresent()) {
            model.addAttribute("diagnostico", diagnostico.get());
            model.addAttribute("trad", trad);
            return "Diagnostico/formDiagnostico";
        }
        attribute.addFlashAttribute("error", "Diagnóstico no encontrado.");
        return "redirect:/diagnosticos";
    }

    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmarDesactivar(@PathVariable Long id, Model model, RedirectAttributes attribute) {
        Optional<Diagnostico> diagnostico = diagnosticoService.buscarPorId(id);
        if (diagnostico.isPresent()) {
            model.addAttribute("diagnostico", diagnostico.get());
            model.addAttribute("trad", trad);
            return "Diagnostico/confirmarDesactivar";
        }
        attribute.addFlashAttribute("error", "Diagnóstico no encontrado.");
        return "redirect:/diagnosticos";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes attribute) {
        diagnosticoService.desactivar(id);
        attribute.addFlashAttribute("warning", "Diagnóstico desactivado correctamente.");
        return "redirect:/diagnosticos";
    }

    @GetMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes attribute) {
        diagnosticoService.reactivar(id);
        attribute.addFlashAttribute("success", "Diagnóstico reactivado correctamente.");
        return "redirect:/diagnosticos";
    }
}