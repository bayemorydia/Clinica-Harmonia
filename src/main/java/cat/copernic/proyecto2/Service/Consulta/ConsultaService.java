package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import java.time.LocalDate;
import java.util.List;

public interface ConsultaService {
    HistorialClinico obtenerOSolucionarHistorial(Long pacienteId);

    // Método principal para finalizar la consulta con todos los datos
    void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento,
            List<PruebaMedica> pruebas, List<LineaReceta> lineas);

    void cambiarEstadoCita(Long citaId, EstadoCita nuevoEstado);

    HistorialClinico obtenerHistorialFiltrado(Long pacienteId, LocalDate inicio, LocalDate fin);

    EpisodioClinico buscarEpisodioPorCita(Cita cita);

    void procesarFinalizacionConsulta(Long citaId, Long diagnosticoId, Tratamiento tratamiento, List<Long> medIds,
            List<String> medDosis, List<Long> pruebaIds);
}