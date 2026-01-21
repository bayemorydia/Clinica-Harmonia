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
import java.util.List;

@Service
public class ConsultaServiceImpl implements ConsultaService {

    @Autowired private HistorialClinicoDAO historialDAO;
    @Autowired private EpisodioClinicoDAO episodioDAO;
    @Autowired private CitaDAO citaDAO;
    @Autowired private MedicamentoDAO medicamentoDAO;
    @Autowired private TratamientoDAO tratamientoDAO;
    @Autowired private RecetaMedicaDAO recetaDAO; // Necesario para persistir la receta

    @Override
    @Transactional
    public void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento, 
                                 List<PruebaMedica> pruebas, List<LineaReceta> lineas) {
        
        Cita cita = citaDAO.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        // 1. Configurar y guardar el Episodio Clínico
        episodio.setMedico(cita.getProfesional());
        episodio.setFecha(LocalDateTime.now());
        episodio.setMotivoConsulta(cita.getMotivo());
        episodio.setPruebasAsignadas(pruebas); // Vincula las pruebas seleccionadas
        
        // Buscamos o creamos el historial del paciente
        HistorialClinico hc = historialDAO.findByPacienteId(cita.getPaciente().getId())
                .orElseGet(() -> {
                    HistorialClinico newHc = new HistorialClinico();
                    newHc.setPaciente(cita.getPaciente());
                    return historialDAO.save(newHc);
                });
        episodio.setHistorial(hc);
        
        EpisodioClinico episodioGuardado = episodioDAO.save(episodio);

        // 2. Guardar Tratamiento (si existe descripción)
        if (tratamiento != null && tratamiento.getDescripcion() != null && !tratamiento.getDescripcion().isBlank()) {
            tratamiento.setEpisodio(episodioGuardado);
            tratamiento.setPaciente(cita.getPaciente());
            tratamiento.setEmpleado(cita.getProfesional());
            if (tratamiento.getFechaInicio() == null) tratamiento.setFechaInicio(java.time.LocalDate.now());
            tratamientoDAO.save(tratamiento);
        }

        // 3. Crear y guardar Receta Médica
        if (lineas != null && !lineas.isEmpty()) {
            RecetaMedica receta = new RecetaMedica();
            receta.setEpisodio(episodioGuardado);
            receta.setPaciente(cita.getPaciente());
            receta.setMedico(cita.getProfesional());
            
            // Importante: Vincular cada línea con su padre (Receta)
            for (LineaReceta lr : lineas) {
                lr.setReceta(receta);
            }
            receta.setLineas(lineas);
            recetaDAO.save(receta);
        }

        // 4. Actualizar estado de la cita
        cita.setEstado(EstadoCita.REALIZADA);
        citaDAO.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinico obtenerOSolucionarHistorial(Long pacienteId) {
        return historialDAO.findByPacienteId(pacienteId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> buscarMedicamentos(String q) {
        return medicamentoDAO.findByNombreContainingIgnoreCase(q);
    }

    @Override
    @Transactional
    public void cambiarEstadoCita(Long id, EstadoCita estado) {
        Cita cita = citaDAO.findById(id).orElseThrow();
        cita.setEstado(estado);
        citaDAO.save(cita);
    }

    @Override
    @Transactional
    public void guardarAtencion(EpisodioClinico e, List<LineaReceta> l, Long c) {
        // Adaptador para llamadas que no incluyen tratamiento o pruebas específicas
        finalizarConsulta(c, e, null, null, l);
    }
}