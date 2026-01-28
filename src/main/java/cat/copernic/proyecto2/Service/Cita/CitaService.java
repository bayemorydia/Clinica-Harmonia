package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CitaService {
    Page<Cita> getCitasPaginadas(String query, EstadoCita estado, int page, int size);

    void saveCita(Cita cita) throws Exception;

    void anularCita(Long id);

    List<Empleado> listarMedicosActivos();

    long countCitasHoy(Long medicoId);

    LocalDate parsearFechaAgenda(String fechaStr);

    Map<String, Long> obtenerResumenCitas(List<Cita> citas);

    long countCitasActivas();

    Cita findCitaById(Long id);

    long countPacientesPendientesPorMedico(Long medicoId);

    List<Cita> obtenerCitasPendientesMedico(Long medicoId);

    List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha);

    List<Cita> listarCitasDeHoyGlobal();

    List<Cita> listarCitasPorDiaGlobal(LocalDate fecha);

    List<Cita> findByPaciente(Paciente paciente);

}