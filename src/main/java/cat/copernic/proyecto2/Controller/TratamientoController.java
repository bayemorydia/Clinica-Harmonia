package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Service.Tratamiento.TratamientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {

    @Autowired
    private TratamientoService tratamientoService;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("tratamientos", tratamientoService.listarTratamientos());
        return "PersonalSanitario/ListaTratamientos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Tratamiento tratamiento) {
        return "PersonalSanitario/FormTratamiento";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Tratamiento tratamiento, BindingResult result) {
        if (result.hasErrors()) {
            return "PersonalSanitario/FormTratamiento";
        }
        tratamientoService.guardar(tratamiento);
        return "redirect:/tratamientos/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(Tratamiento tratamiento, Model model) {
        tratamiento = tratamientoService.encontrarTratamiento(tratamiento);
        model.addAttribute("tratamiento", tratamiento);
        return "PersonalSanitario/FormTratamiento";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(Tratamiento tratamiento) {
        tratamientoService.eliminar(tratamiento);
        return "redirect:/tratamientos/listar";
    }
}
