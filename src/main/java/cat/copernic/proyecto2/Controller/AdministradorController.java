package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdministradorController {
    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private CitaService citaService;

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("totalPacientesActivos", pacienteService.countPacientesActivos());
        model.addAttribute("totalCitasActivas", citaService.countCitasActivas());
        model.addAttribute("totalEmpleados", empleadoService.ContarEmpleados());

        return "Administrador/home";
    }

}