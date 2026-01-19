package cat.copernic.proyecto2.Service.Empleado.Turno;

import cat.copernic.proyecto2.DAO.JPA.TurnoDAO;
import cat.copernic.proyecto2.Domain.Administración.Turno;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnoServiceImpl implements TurnoService {
    @Autowired
    private TurnoDAO turnoRepository;

    @Override
    public void CrearTurnos() {
        if(turnoRepository.count() == 0) {
            if(!turnoRepository.findByNombre("DIURNO").isPresent()) {
                Turno turno = new Turno();
                turno.setNombre("DIURNO");
                turnoRepository.save(turno);
            }

            // Crear NOCTURNO solo si no existe
            if(!turnoRepository.findByNombre("NOCTURNO").isPresent()) {
                Turno turno2 = new Turno();
                turno2.setNombre("NOCTURNO");
                turnoRepository.save(turno2);
            }
        }
    }

    @Override
    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }


}
