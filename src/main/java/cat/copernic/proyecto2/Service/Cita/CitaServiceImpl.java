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
import java.time.LocalDateTime;
import java.util.Optional;

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
}