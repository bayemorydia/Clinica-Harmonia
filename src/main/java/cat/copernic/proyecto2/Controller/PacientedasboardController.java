package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/paciente")
@RequiredArgsConstructor
public class PacientedasboardController {

    private final PacienteService pacienteService;
    private final TraduccionService traduccionService;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true) // Mantiene la sesión abierta durante este método
    public String verDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        Paciente paciente = pacienteService.buscarPorUsername(userDetails.getUsername());

        if (paciente == null) {
            return "redirect:/login?error=perfil-no-encontrado";
        }

        // --- SOLUCIÓN CLAVE: Cargar las colecciones dentro de la transacción ---
        // Al llamar a .size() o crear una nueva lista, forzamos a Hibernate a traer los datos AHORA.
        List<?> citas = (paciente.getCitas() != null) ? new ArrayList<>(paciente.getCitas()) : new ArrayList<>();

        int numEpisodios = 0;
        if (paciente.getHistorial() != null && paciente.getHistorial().getEpisodios() != null) {
            numEpisodios = paciente.getHistorial().getEpisodios().size();
        }

        model.addAttribute("paciente", paciente);
        model.addAttribute("listaCitas", citas); // Pasamos la lista ya cargada
        model.addAttribute("totalCitas", citas.size());
        model.addAttribute("totalEpisodios", numEpisodios);
        model.addAttribute("trad", traduccionService);
        model.addAttribute("pacienteLogueado", paciente);

        return "DashboardPaciente/dashboard";
    }

    @GetMapping("/perfil/{id}")
    public String verDetallePaciente(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.findPacienteById(id);
        if (paciente == null)
            return "redirect:/pacientes";
        model.addAttribute("paciente", paciente);
        model.addAttribute("edad", pacienteService.getEdadPaciente(paciente));
        model.addAttribute("trad", traduccionService);
        return "Paciente/detallePaciente";
    }
}