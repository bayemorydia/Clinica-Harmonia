package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.*;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

@Controller
@RequestMapping("/nominas")
public class NominaController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private NominaService nominaService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TraduccionService traduccionService;

    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

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
            model.addAttribute("error", "Empleado no encontrado");
            return "redirect:/nominas";
        }
        Nomina nomina = new Nomina();
        nomina.setEmpleado(empleado);
        nomina.setSalarioBase(empleado.getSalarioBase() != null ? empleado.getSalarioBase() : BigDecimal.ZERO);
        nomina.setComplementos(BigDecimal.ZERO);
        nomina.setDeducciones(BigDecimal.ZERO);
        nomina.setTotalPagar(nomina.getSalarioBase());
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
        Empleado empleado = empleadoService.obtenerPorId(nomina.getEmpleado().getId());
        if (empleado == null) {
            model.addAttribute("error", "Empleado no encontrado");
            return "Nomina/crear";
        }
        if (nominaService.existeNominaMes(empleado.getId(), nomina.getPeriodo())) {
            model.addAttribute("error", "Ya existe una nómina para este período");
            model.addAttribute("empleado", empleado);
            return "Nomina/crear";
        }
        if (nomina.getSalarioBase() == null)
            nomina.setSalarioBase(BigDecimal.ZERO);
        if (nomina.getComplementos() == null)
            nomina.setComplementos(BigDecimal.ZERO);
        if (nomina.getDeducciones() == null)
            nomina.setDeducciones(BigDecimal.ZERO);
        nomina.setTotalPagar(nomina.getSalarioBase()
                .add(nomina.getComplementos())
                .subtract(nomina.getDeducciones()));
        nominaService.saveNomina(nomina);
        return "redirect:/empleados/nominas/" + nomina.getEmpleado().getId();
    }


    @PostMapping("/actualizar")
    public String actualizarNomina(@RequestParam Long id,
            @Valid @ModelAttribute("nomina") Nomina nomina,
            BindingResult result,
            Model model) {
        model.addAttribute("trad", traduccionService);
        if (result.hasErrors()) {
            return "Nomina/detalle";
        }
        if (nomina.getSalarioBase() == null)
            nomina.setSalarioBase(BigDecimal.ZERO);
        if (nomina.getComplementos() == null)
            nomina.setComplementos(BigDecimal.ZERO);
        if (nomina.getDeducciones() == null)
            nomina.setDeducciones(BigDecimal.ZERO);
        nomina.setTotalPagar(nomina.getSalarioBase()
                .add(nomina.getComplementos())
                .subtract(nomina.getDeducciones()));
        nominaService.updateNomina(id, nomina);
        return "redirect:/listarNominas";
    }
//hola me llamo baye
    @GetMapping("/imprimir/{id}")
    public ResponseEntity<byte[]> imprimirNomina(@PathVariable Long id) {
        Nomina nomina = nominaService.findById(id);
        byte[] pdf = nominaService.generarPdfNomina(nomina);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=nomina_" + id + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/DetalleNomina/{id}")
    public String detalleNomina(@PathVariable("id") Long id, Model model, Authentication auth) {
        Nomina nomina = nominaService.findById(id);
        if (nomina == null) {
            model.addAttribute("error", "La nómina no existe");
            return "redirect:/nominas";
        }
        Empleado empleadoDueno = nomina.getEmpleado();
        if (auth != null) {
            Empleado usuarioNavegando = empleadoService.obtenerPorUsername(auth.getName());
            model.addAttribute("medicoLogueado", usuarioNavegando);
        }
        model.addAttribute("nomina", nomina);
        model.addAttribute("empleado", empleadoDueno);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "nominas");
        return "Nomina/detalleNomina";
    }

    @GetMapping("/exportar")
    @ResponseBody
    public CompletableFuture<ResponseEntity<byte[]>> exportarNominas(@RequestParam String periodo) {
        return nominaService.generarReporteAsync(periodo)
                .thenApply(bytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDisposition(ContentDisposition.attachment()
                            .filename("Nominas_" + periodo + ".csv").build());
                    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
                });
    }
}
