package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public interface CitaService {
    Page<Cita> getCitasPaginadas(String query, int page, int size);
    void saveCita(Cita cita) throws Exception;
    void anularCita(Long id);
    long countCitasActivas();
    Cita findCitaById(Long id);
    long countPacientesPendientesPorMedico(Long medicoId);
    long countCitasHoyPorMedico(Long medicoId, LocalDateTime inicio, LocalDateTime fin);
    List<Cita> obtenerCitasPendientesMedico(Long medicoId);
    Map<LocalDate, Long> obtenerResumenAgendaAnual(Long medicoId, int anio);
    List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha);
}
