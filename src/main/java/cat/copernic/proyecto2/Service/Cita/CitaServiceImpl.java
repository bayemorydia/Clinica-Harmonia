package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.CitaDAO;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaDAO citaDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Cita> getAllCitas() {
        return (List<Cita>) citaDAO.findAll();
    }

    @Override
    @Transactional
    public void saveCita(Cita cita) throws Exception {
        LocalDateTime inicio = cita.getFechaHora();
        // 1. Bloqueo de fechas pasadas en el servidor
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new Exception("No se pueden programar citas en fechas o horas pasadas.");
        }
        // 2. Horario: 8am a 5pm
        int hora = inicio.getHour();
        if (hora < 8 || hora >= 17) {
            throw new Exception("Horario clínico: 08:00 a 17:00.");
        }
        // 3. Forzar minutos a cero (Limpieza de datos)
        cita.setFechaHora(inicio.withMinute(0).withSecond(0).withNano(0));
        LocalDateTime inicioLimpio = cita.getFechaHora();
        // 4. Unicidad
        if (citaDAO.findByPacienteAndFechaHora(cita.getPaciente(), inicioLimpio).isPresent()) {
            throw new Exception("El paciente ya tiene cita a esa hora.");
        }
        if (citaDAO.findByNombreMedicoAndFechaHora(cita.getNombreMedico(), inicioLimpio).isPresent()) {
            throw new Exception("El médico ya tiene otra cita a esa hora.");
        }
        citaDAO.save(cita);
    }

    @Override
    public Cita findCitaById(Long id) {
        return citaDAO.findById(id).orElse(null);
    }

    @Override
    public void deleteCita(Long id) {
        citaDAO.deleteById(id);
    }
}