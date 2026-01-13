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

import java.math.BigDecimal;
import java.util.List;

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

    // ---------------------------
    // MÉTODO AUXILIAR CARGAR ROLES
    // ---------------------------
    private void cargarRoles(Model model) {
        Pageable pageable = PageRequest.of(0, 200);
        model.addAttribute("roles", rolService.listarRoles(pageable).getContent());
    }

    // ---------------------------
    // LISTAR NÓMINAS
    // ---------------------------
    @GetMapping("")
    public String listarNominas(Model model) {
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        model.addAttribute("nominas", nominaService.getAllNominas());
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        return "Nomina/listarNominas";
    }

    // ---------------------------
    // FORMULARIO NUEVA NÓMINA
    // ---------------------------
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

    // ---------------------------
    // GUARDAR NÓMINA
    // ---------------------------
    @PostMapping("/guardar")
    public String guardarNomina(@Valid @ModelAttribute("nomina") Nomina nomina,
                                BindingResult result,
                                Model model) {
        model.addAttribute("trad", traduccionService);

        // Validar errores de formulario
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

        // Evitar nóminas duplicadas
        if (nominaService.existeNominaMes(empleado.getId(), nomina.getPeriodo())) {
            model.addAttribute("error", "Ya existe una nómina para este período");
            model.addAttribute("empleado", empleado);
            return "Nomina/crear";
        }

        // Inicializar campos null
        if (nomina.getSalarioBase() == null) nomina.setSalarioBase(BigDecimal.ZERO);
        if (nomina.getComplementos() == null) nomina.setComplementos(BigDecimal.ZERO);
        if (nomina.getDeducciones() == null) nomina.setDeducciones(BigDecimal.ZERO);

        // Calcular total automáticamente
        nomina.setTotalPagar(nomina.getSalarioBase()
                .add(nomina.getComplementos())
                .subtract(nomina.getDeducciones()));

        nominaService.saveNomina(nomina);
        return "redirect:/listarNominas";
    }

    // ---------------------------
    // DETALLE DEL EMPLEADO
    // ---------------------------
    @GetMapping("/detalle/{id}")
    public String detalleEmpleado(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Empleado empleado = empleadoService.obtenerPorId(id);
        if (empleado == null) {
            model.addAttribute("error", "Empleado no encontrado");
            return "redirect:/listarNominas";
        }

        // Paginación de nóminas por empleado
        Page<Nomina> nominasPage = nominaService.getNominasPorEmpleado(id, PageRequest.of(page, size));

        model.addAttribute("empleado", empleado);
        model.addAttribute("empleadoNominas", nominasPage.getContent()); // solo la página actual
        model.addAttribute("currentPage", nominasPage.getNumber());
        model.addAttribute("totalPages", nominasPage.getTotalPages());
        model.addAttribute("totalElements", nominasPage.getTotalElements());
        model.addAttribute("size", nominasPage.getSize());
        model.addAttribute("trad", traduccionService);

        // Cargar roles si tu plantilla los necesita
        cargarRoles(model);

        return "Nomina/detalle";
    }

    // ---------------------------
    // ACTUALIZAR NÓMINA
    // ---------------------------
    @PostMapping("/actualizar")
    public String actualizarNomina(@RequestParam Long id,
                                   @Valid @ModelAttribute("nomina") Nomina nomina,
                                   BindingResult result,
                                   Model model) {
        model.addAttribute("trad", traduccionService);

        if (result.hasErrors()) {
            return "Nomina/detalle";
        }

        // Calcular total antes de actualizar
        if (nomina.getSalarioBase() == null) nomina.setSalarioBase(BigDecimal.ZERO);
        if (nomina.getComplementos() == null) nomina.setComplementos(BigDecimal.ZERO);
        if (nomina.getDeducciones() == null) nomina.setDeducciones(BigDecimal.ZERO);
        nomina.setTotalPagar(nomina.getSalarioBase()
                .add(nomina.getComplementos())
                .subtract(nomina.getDeducciones()));

        nominaService.updateNomina(id, nomina);
        return "redirect:/listarNominas";
    }

    // ---------------------------
    // IMPRIMIR NÓMINA (PDF)
    // ---------------------------
    @GetMapping("/imprimir/{id}")
    public ResponseEntity<byte[]> imprimirNomina(@PathVariable Long id) {
        Nomina nomina = nominaService.findById(id); // tu método para obtener la nómina
        byte[] pdf = nominaService.generarPdfNomina(nomina);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=nomina_" + id + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    @GetMapping("/DetalleNomina/{id}")
    public String detalleNomina(@PathVariable("id") Long id, Model model) {
        // Obtenemos la nómina por ID
        Nomina nomina = nominaService.findById(id);
        if (nomina == null) {
            model.addAttribute("error", "La nómina no existe");
            return "redirect:/listarNominas";
        }

        // Obtenemos el empleado relacionado
        Empleado empleado = nomina.getEmpleado();

        // Pasamos datos al modelo
        model.addAttribute("nomina", nomina);
        model.addAttribute("empleado", empleado);
        model.addAttribute("trad", traduccionService);

        return "Nomina/DetalleNomina"; // Thymeleaf
    }

}

