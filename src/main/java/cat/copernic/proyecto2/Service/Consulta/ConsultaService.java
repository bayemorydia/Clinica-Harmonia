package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import java.util.List;

public interface ConsultaService {
    HistorialClinico obtenerOSolucionarHistorial(Long pacienteId);
    void guardarAtencion(EpisodioClinico episodio, List<LineaReceta> lineas, Long citaId);
    List<Medicamento> buscarMedicamentos(String query);
    void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento, List<LineaReceta> lineas);
    void cambiarEstadoCita(Long citaId, EstadoCita nuevoEstado);
}