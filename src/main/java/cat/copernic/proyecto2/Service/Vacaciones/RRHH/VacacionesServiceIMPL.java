package cat.copernic.proyecto2.Service.Vacaciones.RRHH;
import cat.copernic.proyecto2.Domain.Administración.Vacaciones;
import cat.copernic.proyecto2.DAO.VacacionesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cat.copernic.proyecto2.Domain.Administración.EstadoVacaciones;

import java.util.List;
import java.util.Optional;

@Service
public class VacacionesServiceIMPL implements VacacionesServiceRRHH {

    @Autowired
    private VacacionesDAO vacacionesRepository;

    // --- Listar todas las solicitudes (para RRHH) ---
    public List<Vacaciones> listarTodasLasVacaciones() {
        return vacacionesRepository.findAll();
    }

    // --- Obtener solicitud por ID ---
    public Vacaciones obtenerPorId(Long id) {
        Optional<Vacaciones> opt = vacacionesRepository.findById(id);
        return opt.orElse(null);
    }
    @Override
    public Vacaciones aprobarVacaciones(Long id) {
        Vacaciones vacaciones = vacacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacaciones no encontradas"));


        vacaciones.setEstado(EstadoVacaciones.ACEPTADA);
        return vacacionesRepository.save(vacaciones);
    }

    @Override
    public Vacaciones denegarVacaciones(Long id) {
        Vacaciones vacaciones = vacacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacaciones no encontradas"));

        if (vacaciones.getEstado() != EstadoVacaciones.PENDIENTE) {
            throw new RuntimeException("Solo se pueden denegar vacaciones pendientes");
        }

        vacaciones.setEstado(EstadoVacaciones.RECHAZADA);
        return vacacionesRepository.save(vacaciones);
    }

    public long contarPendientes() {
        return vacacionesRepository.contarPendiente();
    }
}
