package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/medicamentos") // Ruta base para este controlador
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    // 1. Listar todos los medicamentos
    @GetMapping("/listar")
    public String listar(Model model) {
        List<Medicamento> listaMedicamentos = medicamentoService.listarMedicamentos();
        model.addAttribute("medicamentos", listaMedicamentos);
        return "farmacia/ListaMedicamentos"; // Retorna el archivo HTML: src/main/resources/templates/farmacia/lista-medicamentos.html
    }

    // 2. Mostrar formulario para añadir un nuevo medicamento
    @GetMapping("/nuevo")
    public String formularioNuevo(Medicamento medicamento) {
        return "farmacia/FormMedicamento"; // Retorna el archivo HTML del formulario
    }

    // 3. Guardar el medicamento (Nuevo o Editado)
    @PostMapping("/guardar")
    public String guardar(Medicamento medicamento) {
        medicamentoService.guardar(medicamento);
        return "redirect:/medicamentos/listar"; // Redirige a la lista después de guardar
    }

    // 4. Editar un medicamento existente
    @GetMapping("/editar/{id}")
    public String editar(Medicamento medicamento, Model model) {
        medicamento = medicamentoService.encontrarMedicamento(medicamento);
        model.addAttribute("medicamento", medicamento);
        return "farmacia/FormMedicamento";
    }

    // 5. Eliminar un medicamento
    @GetMapping("/eliminar/{id}")
    public String eliminar(Medicamento medicamento) {
        medicamentoService.eliminar(medicamento);
        return "redirect:/medicamentos/listar";
    }
}
