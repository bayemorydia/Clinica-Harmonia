package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Service.Tratamiento.TratamientoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {

    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("tratamientos", tratamientoService.listarTratamientos());
        model.addAttribute("trad", traduccionService);
        return "PersonalSanitario/ListaTratamientos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Tratamiento tratamiento, Model model) {
        model.addAttribute("trad", traduccionService);
        return "PersonalSanitario/FormTratamiento";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Tratamiento tratamiento, BindingResult result, Model model) {
        
        // --- VALIDACIÓN DE NOMBRE DUPLICADO ---
        if (tratamientoService.existeNombre(tratamiento.getNombre(), tratamiento.getId())) {
            result.rejectValue("nombre", "error.nombre", "Ya existe un tratamiento con este nombre.");
        }

        // --- SI HAY ERRORES, VOLVEMOS AL FORMULARIO ---
        if (result.hasErrors()) {
            model.addAttribute("trad", traduccionService);
            return "PersonalSanitario/FormTratamiento";
        }
        
        tratamientoService.guardar(tratamiento);
        return "redirect:/tratamientos/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setId(id);
        tratamiento = tratamientoService.encontrarTratamiento(tratamiento);
        model.addAttribute("tratamiento", tratamiento);
        model.addAttribute("trad", traduccionService);
        return "PersonalSanitario/FormTratamiento";
    }

    @GetMapping("/desactivar/{id}")
    public String mostrarConfirmacionDesactivar(@PathVariable("id") Long id, Model model) {
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setId(id);
        tratamiento = tratamientoService.encontrarTratamiento(tratamiento);
        model.addAttribute("tratamiento", tratamiento);
        model.addAttribute("trad", traduccionService);
        return "PersonalSanitario/confirmarDesactivarTratamiento";
    }

    @PostMapping("/desactivar/confirmado")
    public String desactivarConfirmado(@RequestParam("id") Long id) {
        Tratamiento t = new Tratamiento();
        t.setId(id);
        t = tratamientoService.encontrarTratamiento(t);
        t.setActivo(false);
        tratamientoService.guardar(t);
        return "redirect:/tratamientos/listar";
    }

    @GetMapping("/activar/{id}")
    public String activarTratamiento(@PathVariable("id") Long id) {
        Tratamiento t = new Tratamiento();
        t.setId(id);
        t = tratamientoService.encontrarTratamiento(t);
        t.setActivo(true);
        tratamientoService.guardar(t);
        return "redirect:/tratamientos/listar";
    }

    // --- SOLUCIÓN AL PROBLEMA DEL MENÚ LATERAL ---
    @GetMapping("")
    public String index() {
        return "redirect:/tratamientos/listar";
    }
}