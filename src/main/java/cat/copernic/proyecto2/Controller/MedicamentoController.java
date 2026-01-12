package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/medicamentos")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoService.listarMedicamentos();
        model.addAttribute("medicamentos", listaMedicamentos);
        return "Farmacia/ListaMedicamentos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Medicamento medicamento) {
        return "Farmacia/FormMedicamento";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Medicamento medicamento, BindingResult result) {
        if (result.hasErrors()) {
            return "Farmacia/FormMedicamento";
        }
        medicamentoService.guardar(medicamento);
        return "redirect:/medicamentos/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(Medicamento medicamento, Model model) {
        medicamento = medicamentoService.encontrarMedicamento(medicamento);
        model.addAttribute("medicamento", medicamento);
        return "Farmacia/FormMedicamento";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(Medicamento medicamento) {
        medicamentoService.eliminar(medicamento);
        return "redirect:/medicamentos/listar";
    }
}