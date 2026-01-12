package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.CitaDAO;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaDAO citaDAO;

    @Override
    @Transactional(readOnly = true)
    public Page<Cita> getCitasPaginadas(String query, int page, int size) {
        // Ordenamos por fecha de forma descendente para ver las más recientes primero
        return citaDAO.findByQuery(query, PageRequest.of(page, size, Sort.by("fechaHora").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Cita findCitaById(Long id) {
        return citaDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCitasActivas() {
        // Retorna todas las que no estén ANULADAS
        return citaDAO.countByEstadoNot(EstadoCita.ANULADA);
    }

    @Override
    @Transactional
    public void saveCita(Cita cita) throws Exception {
        // 1. Forzar "De hora en hora" (Minutos, segundos y nanos a 0)
        if (cita.getFechaHora() != null) {
            cita.setFechaHora(cita.getFechaHora().withMinute(0).withSecond(0).withNano(0));
        } else {
            throw new Exception("La fecha y hora son obligatorias.");
        }

        // 2. Validar fecha futura (Solo para citas nuevas)
        if (cita.getId() == null && cita.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new Exception("No se pueden programar citas en el pasado.");
        }

        // 3. Evitar duplicados del Paciente
        // Buscamos si existe una cita para ese paciente a esa hora que no esté anulada
        Optional<Cita> citaExistentePaciente = citaDAO.findByPacienteAndFechaHoraAndEstadoNot(
                cita.getPaciente(), cita.getFechaHora(), EstadoCita.ANULADA);

        if (citaExistentePaciente.isPresent()) {
            // Si es nueva cita O si es edición pero la cita encontrada tiene un ID distinto
            // a la nuestra
            if (cita.getId() == null || !citaExistentePaciente.get().getId().equals(cita.getId())) {
                throw new Exception("El paciente ya tiene una cita programada para esta hora.");
            }
        }

        // 4. Evitar duplicados del Médico (Profesional)
        Optional<Cita> citaExistenteMedico = citaDAO.findByProfesionalAndFechaHoraAndEstadoNot(
                cita.getProfesional(), cita.getFechaHora(), EstadoCita.ANULADA);

        if (citaExistenteMedico.isPresent()) {
            if (cita.getId() == null || !citaExistenteMedico.get().getId().equals(cita.getId())) {
                throw new Exception("El profesional ya tiene otra cita asignada a esta hora.");
            }
        }

        citaDAO.save(cita);
    }

    @Override
    @Transactional
    public void anularCita(Long id) {
        Cita cita = citaDAO.findById(id).orElse(null);
        if (cita != null) {
            cita.setEstado(EstadoCita.ANULADA); // Marcado lógico (Soft-delete)
            citaDAO.save(cita);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPacientesPendientesPorMedico(Long medicoId) {
        // Contamos pacientes únicos con citas en estado PENDIENTE para este médico
        return citaDAO.countPacientesConCitasPendientes(medicoId, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCitasHoyPorMedico(Long medicoId, LocalDateTime inicio, LocalDateTime fin) {
        // Citas de hoy que NO estén anuladas para este médico
        return citaDAO.countByProfesionalIdAndFechaHoraBetweenAndEstadoNot(
                medicoId, inicio, fin, EstadoCita.ANULADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasPendientesMedico(Long medicoId) {
        // Listado de citas futuras/pendientes para la tabla
        return citaDAO.findByProfesionalIdAndEstadoOrderByFechaHoraAsc(medicoId, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> obtenerResumenAgendaAnual(Long medicoId, int anio) {
        LocalDateTime inicio = LocalDateTime.of(anio, 1, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(anio, 12, 31, 23, 59);

        List<Cita> citas = citaDAO.findByProfesionalIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
                medicoId, inicio, fin, EstadoCita.ANULADA);

        // Agrupamos por fecha para contar cuántas citas hay por día
        return citas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFechaHora().toLocalDate(),
                        Collectors.counting()));
    }

    @Override
    public List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);
        return citaDAO.findByProfesionalIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
                medicoId, inicio, fin, EstadoCita.ANULADA);
    }
}