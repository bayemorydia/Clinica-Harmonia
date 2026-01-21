package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
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
}