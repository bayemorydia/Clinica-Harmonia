package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/medicamentos")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoService.listarMedicamentos();
        model.addAttribute("medicamentos", listaMedicamentos);
        model.addAttribute("trad", traduccionService); 
        return "Farmacia/ListaMedicamentos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Medicamento medicamento, Model model) {
        model.addAttribute("trad", traduccionService);
        return "Farmacia/FormMedicamento";
    }

    // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
    @PostMapping("/guardar")
    public String guardar(@Valid Medicamento medicamento, BindingResult result, Model model) {
        
        // 1. Validar si el nombre ya existe
        if (medicamentoService.existeNombre(medicamento.getNombre(), medicamento.getId())) {
            result.rejectValue("nombre", "error.nombre", "El nombre de este medicamento ya está registrado.");
        }

        // 2. Validar si el código ya existe
        if (medicamentoService.existeCodigo(medicamento.getCodigo(), medicamento.getId())) {
            result.rejectValue("codigo", "error.codigo", "Este código de referencia ya existe.");
        }

        // 3. Comprobar errores (tanto los de arriba como los de @NotBlank, etc.)
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "Farmacia/FormMedicamento";
        }
        
        medicamentoService.guardar(medicamento);
        return "redirect:/medicamentos/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(Medicamento medicamento, Model model) {
        medicamento = medicamentoService.encontrarMedicamento(medicamento);
        model.addAttribute("medicamento", medicamento);
        model.addAttribute("trad", traduccionService);
        return "Farmacia/FormMedicamento";
    }

    @GetMapping("/desactivar/{id}")
    public String mostrarConfirmacionDesactivar(Medicamento medicamento, Model model) {
        medicamento = medicamentoService.encontrarMedicamento(medicamento);
        model.addAttribute("medicamento", medicamento);
        model.addAttribute("trad", traduccionService);
        return "Farmacia/confirmarDesactivarMedicamento";
    }

    @PostMapping("/desactivar/confirmado")
    public String desactivarConfirmado(@RequestParam("id") Long id) {
        Medicamento med = new Medicamento();
        med.setId(id);
        med = medicamentoService.encontrarMedicamento(med);
        med.setActivo(false); 
        medicamentoService.guardar(med);
        return "redirect:/medicamentos/listar";
    }

    @GetMapping("/activar/{id}")
    public String activarMedicamento(@PathVariable("id") Long id) {
        Medicamento med = new Medicamento();
        med.setId(id);
        med = medicamentoService.encontrarMedicamento(med);
        med.setActivo(true);
        medicamentoService.guardar(med);
        return "redirect:/medicamentos/listar";
    }

    @GetMapping("")
    public String index() {
        return "redirect:/medicamentos/listar";
    }
}