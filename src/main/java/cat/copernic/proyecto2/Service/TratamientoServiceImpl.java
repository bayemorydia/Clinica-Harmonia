package cat.copernic.proyecto2.Service;

import cat.copernic.proyecto2.DAO.TratamientoDAO;
import cat.copernic.proyecto2.Domain.Personal_sanitario.Tratamiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TratamientoServiceImpl implements TratamientoService {

    @Autowired
    private TratamientoDAO tratamientoDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Tratamiento> listarTratamientos() {
        return (List<Tratamiento>) tratamientoDAO.findAll();
    }

    @Override
    @Transactional
    public void guardar(Tratamiento tratamiento) {
        tratamientoDAO.save(tratamiento);
    }

    @Override
    @Transactional
    public void eliminar(Tratamiento tratamiento) {
        tratamientoDAO.delete(tratamiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Tratamiento encontrarTratamiento(Tratamiento tratamiento) {
        return tratamientoDAO.findById(tratamiento.getId()).orElse(null);
    }
}
