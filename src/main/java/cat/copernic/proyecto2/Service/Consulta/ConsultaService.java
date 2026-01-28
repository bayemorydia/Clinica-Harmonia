package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;

import java.time.LocalDate;
import java.util.List;

public interface ConsultaService {
    HistorialClinico obtenerOSolucionarHistorial(Long pacienteId);

    // Método principal para finalizar la consulta con todos los datos
    void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento,
            List<PruebaMedica> pruebas, List<LineaReceta> lineas);

    void cambiarEstadoCita(Long citaId, EstadoCita nuevoEstado);

    List<Medicamento> buscarMedicamentos(String query);

    // Método de soporte para compatibilidad con llamadas simplificadas
    void guardarAtencion(EpisodioClinico episodio, List<LineaReceta> lineas, Long citaId);

    HistorialClinico obtenerHistorialFiltrado(Long pacienteId, LocalDate inicio, LocalDate fin);

    EpisodioClinico buscarEpisodioPorCita(Cita cita);

    void procesarFinalizacionConsulta(Long citaId, Long diagnosticoId, Tratamiento tratamiento, List<Long> medIds,
            List<String> medDosis, List<Long> pruebaIds);

    void procesarConsultaLegacy(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento, List<Long> pruebasIds,
            List<String> medNombre, List<String> medDosis);
}