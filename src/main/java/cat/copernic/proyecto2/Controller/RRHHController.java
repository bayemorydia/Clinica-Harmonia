package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/RRHH")
public class RRHHController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TraduccionService traduccionService;

    // ==========================
    // DASHBOARD PRINCIPAL RRHH
    // ==========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Contadores
        long totalEmpleados = empleadoService.listarTodos().size();
        long empleadosActivos = empleadoService.listarTodos().stream().filter(Empleado::getActiva).count();
        long empleadosInactivos = totalEmpleados - empleadosActivos;

        // Aquí podrías tener servicio de Nóminas para contar pendientes
        long nominasPendientes = 0; // placeholder

        // Envío de datos al modelo
        model.addAttribute("totalEmpleados", totalEmpleados);
        model.addAttribute("empleadosActivos", empleadosActivos);
        model.addAttribute("empleadosInactivos", empleadosInactivos);
        model.addAttribute("nominasPendientes", nominasPendientes);

        model.addAttribute("trad", traduccionService);

        return "rrhh/dashboard"; // tu HTML dashboard RRHH
    }

    // ==========================
    // LISTADO COMPLETO DE EMPLEADOS
    // ==========================
    @GetMapping("/empleados")
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("trad", traduccionService);
        model.addAttribute("currentPage", 0);
        model.addAttribute("size", 10);
        return "rrhh/listarEmpleados"; // vista exclusiva RRHH
    }

    // ==========================
    // LISTADO DE BAJAS DE EMPLEADOS
    // ==========================
    @GetMapping("/bajas")
    public String listarBajas(Model model) {
        model.addAttribute("empleadosInactivos", empleadoService.listarTodos().stream()
                .filter(e -> !e.getActiva())
                .toList());
        model.addAttribute("trad", traduccionService);
        return "rrhh/bajasEmpleados"; // vista exclusiva RRHH
    }

    // ==========================
    // GESTIÓN DE NÓMINAS
    // ==========================
    @GetMapping("/nominas")
    public String listarNominas(Model model) {
        // Aquí podrías implementar servicio de nóminas, similar a EmpleadoController
        model.addAttribute("trad", traduccionService);
        return "rrhh/nominas"; // vista de nóminas RRHH
    }
}
