package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.DAO.JPA.*;
import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
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

    @Override
    @Transactional
    public void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento,
            List<PruebaMedica> pruebas, List<LineaReceta> lineas) {

        Cita cita = citaDAO.findById(citaId).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // 1. Asegurar Historial Clínico del Paciente
        HistorialClinico historial = historialDAO.findByPacienteId(cita.getPaciente().getId())
                .orElseGet(() -> {
                    HistorialClinico h = new HistorialClinico();
                    h.setPaciente(cita.getPaciente());
                    h.setEpisodios(new ArrayList<>());
                    return historialDAO.save(h);
                });

        // 2. Configurar el Episodio actual
        episodio.setHistorial(historial);
        episodio.setMedico(cita.getProfesional());
        episodio.setFecha(LocalDateTime.now());

        // 3. Vincular Tratamiento
        if (tratamiento != null && tratamiento.getDescripcion() != null && !tratamiento.getDescripcion().isBlank()) {
            tratamiento.setEpisodio(episodio);
            if (episodio.getTratamientos() == null)
                episodio.setTratamientos(new ArrayList<>());
            episodio.getTratamientos().add(tratamiento);
        }

        // 4. Vincular Pruebas Médicas
        if (pruebas != null && !pruebas.isEmpty()) {
            if (episodio.getPruebasAsignadas() == null)
                episodio.setPruebasAsignadas(new ArrayList<>());
            episodio.getPruebasAsignadas().addAll(pruebas);
        }

        // 5. Vincular Receta Médica y sus líneas (capturando precio unitario)
        if (lineas != null && !lineas.isEmpty()) {
            RecetaMedica receta = new RecetaMedica();
            receta.setEpisodio(episodio);
            receta.setPaciente(cita.getPaciente());
            receta.setMedico(cita.getProfesional());
            receta.setFechaEmision(LocalDate.now());

            for (LineaReceta linea : lineas) {
                linea.setReceta(receta);
                // Si el precio viene nulo desde la vista, lo buscamos en el catálogo de
                // medicamentos
                if (linea.getPrecioUnitario() == null) {
                    List<Medicamento> meds = medicamentoDAO.findByNombreContainingIgnoreCase(linea.getMedicamento());
                    if (!meds.isEmpty()) {
                        // Se usa getPrecioUnitario() corregido
                        linea.setPrecioUnitario(meds.get(0).getPrecioUnitario());
                    } else {
                        linea.setPrecioUnitario(0.0);
                    }
                }
            }
            receta.setLineas(new ArrayList<>(lineas));
            if (episodio.getRecetas() == null)
                episodio.setRecetas(new ArrayList<>());
            episodio.getRecetas().add(receta);
        }

        // 6. Guardado en cascada del Episodio (guardará tratamientos y recetas
        // asociadas)
        episodioDAO.save(episodio);

        // 7. Actualizar el estado de la cita
        cita.setEstado(EstadoCita.REALIZADA);
        citaDAO.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinico obtenerOSolucionarHistorial(Long pacienteId) {
        return historialDAO.findByPacienteId(pacienteId).orElse(null);
    }

    @Override
    public List<Medicamento> buscarMedicamentos(String q) {
        return medicamentoDAO.findByNombreContainingIgnoreCase(q);
    }

    @Override
    @Transactional
    public void cambiarEstadoCita(Long id, EstadoCita st) {
        citaDAO.findById(id).ifPresent(c -> {
            c.setEstado(st);
            citaDAO.save(c);
        });
    }

    @Override
    @Transactional
    public void guardarAtencion(EpisodioClinico e, List<LineaReceta> l, Long c) {
        finalizarConsulta(c, e, null, null, l);
    }
}