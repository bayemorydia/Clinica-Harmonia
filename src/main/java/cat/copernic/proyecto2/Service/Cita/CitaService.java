package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CitaService {
    Page<Cita> getCitasPaginadas(String query, EstadoCita estado, int page, int size);

    void saveCita(Cita cita) throws Exception;

    void anularCita(Long id);

    List<Empleado> listarMedicosActivos();

    long countCitasHoy(Long medicoId);

    LocalDate parsearFechaAgenda(String fechaStr);

    long countCitasActivas();

    Cita findCitaById(Long id);

    long countPacientesPendientesPorMedico(Long medicoId);

    long countCitasHoyPorMedico(Long medicoId, LocalDateTime inicio, LocalDateTime fin);

    List<Cita> obtenerCitasPendientesMedico(Long medicoId);

    Map<LocalDate, Long> obtenerResumenAgendaAnual(Long medicoId, int anio);

    List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha);

    List<Cita> listarCitasDeHoyGlobal();

    List<Cita> listarCitasPorDiaGlobal(LocalDate fecha);

    List<Cita> findByPaciente(Paciente paciente);

    // Nuevo método para mover lógica de creación inicial fuera del controller
    Cita prepararNuevaCita();
}