package cat.copernic.proyecto2.Service.Vacaciones.RRHH;

import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.DAO.JPA.VacacionesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;

import java.util.List;
import java.util.Optional;

@Service
public class VacacionesServiceRRHHImpl implements VacacionesServiceRRHH {

    @Autowired
    private VacacionesDAO vacacionesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Vacaciones> listarTodasLasVacaciones() {
        return vacacionesRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Vacaciones obtenerPorId(Long id) {
        Optional<Vacaciones> opt = vacacionesRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    @Transactional
    public Vacaciones aprobarVacaciones(Long id) {
        Vacaciones vacaciones = vacacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacaciones no encontradas"));

        // Lógica movida del Controller: Solo se puede aprobar si está pendiente
        if (vacaciones.getEstado() != EstadoVacaciones.PENDIENTE) {
            throw new RuntimeException("Solo se pueden aprobar vacaciones en estado pendiente");
        }

        vacaciones.setEstado(EstadoVacaciones.ACEPTADA);
        return vacacionesRepository.save(vacaciones);
    }

    @Override
    @Transactional
    public Vacaciones denegarVacaciones(Long id) {
        Vacaciones vacaciones = vacacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacaciones no encontradas"));

        // Lógica movida del Controller: Solo se puede denegar si está pendiente
        if (vacaciones.getEstado() != EstadoVacaciones.PENDIENTE) {
            throw new RuntimeException("Solo se pueden denegar vacaciones en estado pendiente");
        }

        vacaciones.setEstado(EstadoVacaciones.RECHAZADA);
        return vacacionesRepository.save(vacaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes() {
        return vacacionesRepository.contarPendiente();
    }
}