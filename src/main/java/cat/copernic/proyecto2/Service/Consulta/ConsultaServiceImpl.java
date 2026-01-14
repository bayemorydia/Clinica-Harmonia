package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.DAO.*;
import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultaServiceImpl implements ConsultaService {

    @Autowired
    private HistorialClinicoDAO historialDAO;
    @Autowired
    private EpisodioClinicoDAO episodioDAO;
    @Autowired
    private CitaDAO citaDAO;
    @Autowired
    private MedicamentoDAO medicamentoDAO;
    @Autowired
    private PacienteDAO pacienteDAO;

    @Override
    @Transactional(readOnly = true)
    public HistorialClinico obtenerOSolucionarHistorial(Long pacienteId) {
        return historialDAO.findByPacienteId(pacienteId)
                .orElseGet(() -> {
                    HistorialClinico nuevo = new HistorialClinico();
                    nuevo.setPaciente(pacienteDAO.findById(pacienteId).orElse(null));
                    nuevo.setEpisodios(new ArrayList<>());
                    return historialDAO.save(nuevo);
                });
    }

    @Override
    @Transactional
    public void guardarAtencion(EpisodioClinico episodio, List<LineaReceta> lineas, Long citaId) {
        // 1. Guardar el episodio
        EpisodioClinico guardado = episodioDAO.save(episodio);

        // 2. Si hay medicinas, crear la receta
        if (lineas != null && !lineas.isEmpty()) {
            RecetaMedica receta = new RecetaMedica();
            receta.setEpisodio(guardado);
            receta.setPaciente(guardado.getHistorial().getPaciente());
            receta.setMedico(guardado.getMedico());
            receta.setLineas(lineas);
            lineas.forEach(l -> l.setReceta(receta));
            // Aquí se guardaría mediante un RecetaDAO si fuera necesario
        }

        // 3. Marcar cita como completada
        Cita cita = citaDAO.findById(citaId).orElseThrow();
        cita.setEstado(EstadoCita.REALIZADA);
        citaDAO.save(cita);
    }

    @Override
    public List<cat.copernic.proyecto2.Domain.Farmacia.Medicamento> buscarMedicamentos(String query) {
        return medicamentoDAO.findByNombreContainingIgnoreCase(query);
    }

    @Override
    @Transactional
    public void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento,
            List<LineaReceta> lineas) {
        Cita cita = citaDAO.findById(citaId).orElseThrow();

        // 1. Asegurar Historial Clínico
        HistorialClinico historial = historialDAO.findByPacienteId(cita.getPaciente().getId())
                .orElseGet(() -> {
                    HistorialClinico h = new HistorialClinico();
                    h.setPaciente(cita.getPaciente());
                    return historialDAO.save(h);
                });

        // 2. Configurar Episodio
        episodio.setHistorial(historial);
        episodio.setMedico(cita.getProfesional());
        episodio.setFecha(LocalDateTime.now());

        if (tratamiento != null && tratamiento.getNombre() != null && !tratamiento.getNombre().isEmpty()) {
            tratamiento.setEpisodio(episodio);
            // IMPORTANTE: Usa una lista mutable
            List<Tratamiento> listaTrat = new ArrayList<>();
            listaTrat.add(tratamiento);
            episodio.setTratamientos(listaTrat);
        }

        // 4. Vincular Receta si hay líneas
        if (lineas != null && !lineas.isEmpty()) {
            RecetaMedica receta = new RecetaMedica();
            receta.setEpisodio(episodio);
            receta.setPaciente(cita.getPaciente());
            receta.setMedico(cita.getProfesional());
            receta.setFechaEmision(LocalDate.now());

            // Establecer la relación bidireccional correctamente
            receta.setLineas(new ArrayList<>(lineas));
            lineas.forEach(l -> l.setReceta(receta));

            List<RecetaMedica> listaRecetas = new ArrayList<>();
            listaRecetas.add(receta);
            episodio.setRecetas(listaRecetas);
        }

        episodioDAO.save(episodio);

        // 5. Actualizar Cita
        cita.setEstado(EstadoCita.REALIZADA);
        citaDAO.save(cita);
    }

    @Override
    @Transactional
    public void cambiarEstadoCita(Long citaId, EstadoCita nuevoEstado) {
        Cita cita = citaDAO.findById(citaId).orElseThrow();
        cita.setEstado(nuevoEstado);
        citaDAO.save(cita);
    }
}
