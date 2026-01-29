package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.*;

@Controller
@RequestMapping("/nominas")
public class NominaController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private NominaService nominaService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping("")
    public String listarNominas(Model model) {
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        model.addAttribute("nominas", nominaService.getAllNominas());
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        return "Nomina/listarNominas";
    }

    @GetMapping("/nuevo/{id}")
    public String mostrarFormularioCrear(@PathVariable("id") Long id, Model model) {
        Empleado empleado = empleadoService.obtenerPorId(id);
        if (empleado == null) {
            return "redirect:/nominas";
        }
        Nomina nomina = nominaService.inicializarNomina(empleado);
        model.addAttribute("empleado", empleado);
        model.addAttribute("nomina", nomina);
        model.addAttribute("trad", traduccionService);
        return "Nomina/crear";
    }

    @PostMapping("/guardar")
    public String guardarNomina(@Valid @ModelAttribute("nomina") Nomina nomina,
                                BindingResult result,
                                Model model) {
        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            if (nomina.getEmpleado() != null) {
                model.addAttribute("empleado", empleadoService.obtenerPorId(nomina.getEmpleado().getId()));
            }
            return "Nomina/crear";
        }

        try {
            // Toda la lógica de validación de duplicados y cálculos se movió al service
            nominaService.guardarNominaProcesada(nomina);
            return "redirect:/empleados/nominas/" + nomina.getEmpleado().getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("empleado", empleadoService.obtenerPorId(nomina.getEmpleado().getId()));
            return "Nomina/crear";
        }
    }

    @GetMapping("/imprimir/{id}")
    public ResponseEntity<byte[]> imprimirNomina(@PathVariable Long id) {
        Nomina nomina = nominaService.findById(id);
        byte[] pdf = nominaService.generarPdfNomina(nomina);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=nomina_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/DetalleNomina/{id}")
    public String detalleNomina(@PathVariable("id") Long id, Model model, Authentication auth) {
        Nomina nomina = nominaService.findById(id);
        if (nomina == null) {
            return "redirect:/nominas";
        }
        
        // La lógica de obtener el usuario navegando también es delegada por consistencia
        if (auth != null) {
            model.addAttribute("medicoLogueado", empleadoService.obtenerPorUsername(auth.getName()));
        }
        
        model.addAttribute("nomina", nomina);
        model.addAttribute("empleado", nomina.getEmpleado());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "nominas");
        return "Nomina/detalleNomina";
    }
}