package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.JPA.CitaDAO;
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
import java.time.temporal.ChronoUnit;
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
    public Page<Cita> getCitasPaginadas(String query, EstadoCita estado, int page, int size) {
        return citaDAO.findByFilters(query, estado, PageRequest.of(page, size, Sort.by("fechaHora").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Cita findCitaById(Long id) {
        return citaDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCitasActivas() {
        return citaDAO.countByEstadoNot(EstadoCita.ANULADA);
    }

    @Override
    @Transactional
    public void saveCita(Cita cita) throws Exception {
        if (cita.getFechaHora() == null)
            throw new Exception("La fecha y hora son obligatorias.");

        LocalDateTime fechaNormalizada = cita.getFechaHora().truncatedTo(ChronoUnit.HOURS);
        cita.setFechaHora(fechaNormalizada);

        if (fechaNormalizada.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS))) {
            throw new Exception("No se pueden agendar citas en el pasado.");
        }

        int diaSemana = fechaNormalizada.getDayOfWeek().getValue();
        if (diaSemana > 5)
            throw new Exception("Solo se pueden agendar citas de lunes a viernes.");

        int hora = fechaNormalizada.getHour();
        if (hora < 8 || hora >= 17)
            throw new Exception("El horario de atención es de 08:00 a 17:00.");
        if (hora == 13)
            throw new Exception("El horario de 13:00 a 14:00 es la hora de almuerzo.");

        Optional<Cita> confP = citaDAO.findByPacienteAndFechaHoraAndEstadoNot(cita.getPaciente(), fechaNormalizada,
                EstadoCita.ANULADA);
        if (confP.isPresent() && (cita.getId() == null || !confP.get().getId().equals(cita.getId()))) {
            throw new Exception("El paciente ya tiene una cita a esta hora.");
        }

        Optional<Cita> confM = citaDAO.findByProfesionalAndFechaHoraAndEstadoNot(cita.getProfesional(),
                fechaNormalizada, EstadoCita.ANULADA);
        if (confM.isPresent() && (cita.getId() == null || !confM.get().getId().equals(cita.getId()))) {
            throw new Exception("El médico ya tiene una cita asignada a esta hora.");
        }

        if (cita.getId() == null)
            cita.setEstado(EstadoCita.PROGRAMADA);
        citaDAO.save(cita);
    }

    @Override
    @Transactional
    public void anularCita(Long id) {
        Cita cita = findCitaById(id);
        // Regla de negocio: Solo se pueden anular citas PROGRAMADAS
        if (cita != null && cita.getEstado() == EstadoCita.PROGRAMADA) {
            cita.setEstado(EstadoCita.ANULADA);
            citaDAO.save(cita);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPacientesPendientesPorMedico(Long medicoId) {
        return citaDAO.countPacientesConCitasPendientes(medicoId, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCitasHoyPorMedico(Long medicoId, LocalDateTime inicio, LocalDateTime fin) {
        return citaDAO.countByProfesionalIdAndFechaHoraBetweenAndEstadoNot(medicoId, inicio, fin, EstadoCita.ANULADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasPendientesMedico(Long medicoId) {
        return citaDAO.findByProfesionalIdAndEstadoOrderByFechaHoraAsc(medicoId, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> obtenerResumenAgendaAnual(Long medicoId, int anio) {
        LocalDateTime inicio = LocalDateTime.of(anio, 1, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(anio, 12, 31, 23, 59);
        return citaDAO
                .findByProfesionalIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(medicoId, inicio, fin,
                        EstadoCita.ANULADA)
                .stream().collect(Collectors.groupingBy(c -> c.getFechaHora().toLocalDate(), Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha) {
        return citaDAO.findByProfesionalIdAndFechaHoraBetweenAndEstadoOrderByFechaHoraAsc(
                medicoId, fecha.atStartOfDay(), fecha.atTime(23, 59, 59), EstadoCita.PROGRAMADA);
    }
}