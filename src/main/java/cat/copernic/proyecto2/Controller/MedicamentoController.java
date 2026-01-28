package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
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
@RequestMapping("/medicamentos")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping({ "", "/listar" })
    public String listar(@RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Medicamento> medsPage = medicamentoService.getMedicamentosPaginados(query, activo, page, size);
        model.addAttribute("medicamentos", medsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medsPage.getTotalPages());
        model.addAttribute("totalElements", medsPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("activo", activo);
        model.addAttribute("trad", traduccionService);
        return "Farmacia/ListaMedicamentos";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Medicamento> medsPage = medicamentoService.getMedicamentosPaginados(query, activo, page, size);
        model.addAttribute("medicamentos", medsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medsPage.getTotalPages());
        model.addAttribute("totalElements", medsPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "Farmacia/ListaMedicamentos :: tablaMedicamentos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        // Lógica de creación delegada
        model.addAttribute("medicamento", medicamentoService.crearNuevo());
        model.addAttribute("trad", traduccionService);
        return "Farmacia/FormMedicamento";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("medicamento", medicamentoService.encontrarMedicamento(id));
        model.addAttribute("trad", traduccionService);
        return "Farmacia/FormMedicamento";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("medicamento") Medicamento medicamento,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "Farmacia/FormMedicamento";
        }
        try {
            medicamentoService.guardar(medicamento);
            flash.addFlashAttribute("success", "Operación realizada con éxito.");
            return "redirect:/medicamentos";
        } catch (RuntimeException e) {
            // El servicio lanza excepciones con mensajes específicos que el controller traduce a la UI
            if ("CODIGO_DUPLICADO".equals(e.getMessage())) {
                result.rejectValue("codigo", "error.medicamento",
                        "Este código ya está registrado en el sistema.");
                model.addAttribute("trad", traduccionService);
                return "Farmacia/FormMedicamento";
            }
            throw e; 
        }
    }

    @GetMapping("/confirmarDesactivar/{id}")
    public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("medicamento", medicamentoService.encontrarMedicamento(id));
        model.addAttribute("trad", traduccionService);
        return "Farmacia/confirmarDesactivarMedicamento";
    }

    @PostMapping("/desactivar")
    public String desactivar(@RequestParam Long id, RedirectAttributes flash) {
        medicamentoService.eliminarLogica(id);
        flash.addFlashAttribute("warning", "Medicamento desactivado.");
        return "redirect:/medicamentos";
    }

    @GetMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        medicamentoService.activarLogica(id);
        flash.addFlashAttribute("success", "Medicamento reactivado.");
        return "redirect:/medicamentos";
    }
}