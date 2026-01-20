package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.PersonalSanitario.EpisodioClinico;
import cat.copernic.proyecto2.Domain.PersonalSanitario.LineaReceta;
import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import cat.copernic.proyecto2.Service.Medicamento.MedicamentoService;
import cat.copernic.proyecto2.Service.Prueba.PruebaService;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Consulta.ConsultaService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import cat.copernic.proyecto2.Service.Empleado.Nomina.NominaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private TraduccionService traduccionService;

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private NominaService nominaService;

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private PruebaService pruebaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "dashboard");
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("medicoLogueado", medico);
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
        model.addAttribute("totalPacientes", citaService.countPacientesPendientesPorMedico(medico.getId()));
        model.addAttribute("citasHoy", citaService.countCitasHoyPorMedico(medico.getId(), inicioHoy, finHoy));
        model.addAttribute("citasPendientes", citaService.obtenerCitasPendientesMedico(medico.getId()));
        return "Medico/dashboard";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable("id") Long id, Model model) {
        model.addAttribute("trad", traduccionService);
        model.addAttribute("activePage", "perfil");
        Empleado medico = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", medico);
        model.addAttribute("medicoLogueado", medico);
        return "Empleado/detalle";
    }

    @GetMapping("/agenda")
    public String mostrarAgenda(Model model, Authentication auth,
            @RequestParam(name = "fecha", required = false) String fechaStr) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        LocalDate hoy = LocalDate.now();
        LocalDate fechaBusqueda;
        try {
            fechaBusqueda = (fechaStr == null) ? hoy : LocalDate.parse(fechaStr);
            if (fechaBusqueda.isBefore(hoy))
                fechaBusqueda = hoy;
        } catch (Exception e) {
            fechaBusqueda = hoy;
        }
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("citas", citaService.obtenerCitasDia(medico.getId(), fechaBusqueda));
        model.addAttribute("fechaActual", fechaBusqueda);
        model.addAttribute("hoy", hoy);
        model.addAttribute("activePage", "agenda");
        model.addAttribute("trad", traduccionService);
        return "Medico/agenda";
    }

    @GetMapping("/atender/{id}")
    public String atenderPaciente(@PathVariable("id") Long id, Model model, Authentication auth) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        model.addAttribute("listaMedicamentos", medicamentoService.listarMedicamentos());
        model.addAttribute("listaTratamientos", pruebaService.listarPruebas());
        Cita cita = citaService.findCitaById(id);
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", cita.getPaciente());
        model.addAttribute("episodio", new EpisodioClinico());
        model.addAttribute("tratamiento", new Tratamiento());
        model.addAttribute("activePage", "agenda");
        model.addAttribute("trad", traduccionService);
        return "Medico/atender";
    }

    @PostMapping("/finalizar-consulta")
    public String guardarConsulta(@RequestParam("citaId") Long citaId,
            @ModelAttribute EpisodioClinico episodio,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(value = "meds[]", required = false) List<String> meds,
            @RequestParam(value = "dosis[]", required = false) List<String> dosis,
            @RequestParam(value = "pruebaTipo[]", required = false) List<String> pruebaTipos,
            @RequestParam(value = "pruebaResultado[]", required = false) List<String> pruebaResultados) {
        List<LineaReceta> lineas = new ArrayList<>();
        if (meds != null) {
            for (int i = 0; i < meds.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setDosis(dosis.get(i));
                lineas.add(lr);
            }
        }
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebaTipos != null) {
            for (int i = 0; i < pruebaTipos.size(); i++) {
                if (!pruebaTipos.get(i).isBlank()) {
                    PruebaMedica pm = new PruebaMedica();
                    pm.setNombre(pruebaTipos.get(i));
                    pruebas.add(pm);
                }
            }
        }
        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);
        return "redirect:/medico/agenda?success=true";
    }

    @GetMapping("/cita/estado/{id}/{estado}")
    public String cambiarEstado(@PathVariable Long id, @PathVariable String estado) {
        consultaService.cambiarEstadoCita(id, EstadoCita.valueOf(estado));
        return "redirect:/medico/agenda";
    }

    @GetMapping("/nominas")
    public String listarMisNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        org.springframework.data.domain.Page<cat.copernic.proyecto2.Domain.Administración.Nomina> paginaNominas = nominaService
                .getNominasPorEmpleado(medico.getId(), org.springframework.data.domain.PageRequest.of(page, size));
        model.addAttribute("medicoLogueado", medico);
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "nominas");
        model.addAttribute("trad", traduccionService);
        model.addAttribute("query", "");
        return "Nomina/listarNominas";
    }

    @GetMapping("/nominas/search")
    public String searchNominas(Model model, Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Empleado medico = empleadoService.obtenerPorUsername(auth.getName());
        org.springframework.data.domain.Page<cat.copernic.proyecto2.Domain.Administración.Nomina> paginaNominas = nominaService
                .getNominasPorEmpleado(medico.getId(), org.springframework.data.domain.PageRequest.of(page, size));
        model.addAttribute("nominas", paginaNominas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaNominas.getTotalPages());
        model.addAttribute("totalElements", paginaNominas.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "Nomina/listarNominas :: tablaNominas";
    }

    @PostMapping("/atender/finalizar")
    public String finalizarConsulta(@RequestParam Long citaId,
            @ModelAttribute EpisodioClinico episodio,
            @ModelAttribute Tratamiento tratamiento,
            @RequestParam(required = false) List<Long> pruebasIds,
            @RequestParam(required = false) List<String> medNombre,
            @RequestParam(required = false) List<String> medDosis,
            @RequestParam(required = false) List<Double> medPrecio) {
        List<PruebaMedica> pruebas = new ArrayList<>();
        if (pruebasIds != null) {
            pruebasIds.forEach(id -> pruebas.add(pruebaService.encontrarPrueba(id)));
        }
        List<LineaReceta> lineas = new ArrayList<>();
        if (medNombre != null) {
            for (int i = 0; i < medNombre.size(); i++) {
                LineaReceta lr = new LineaReceta();
                lr.setDosis(medDosis.get(i));
                lineas.add(lr);
            }
        }
        consultaService.finalizarConsulta(citaId, episodio, tratamiento, pruebas, lineas);
        return "redirect:/medico/dashboard?success";
    }
}