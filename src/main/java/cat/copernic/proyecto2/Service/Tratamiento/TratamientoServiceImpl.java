package cat.copernic.proyecto2.Service.Tratamiento;

import cat.copernic.proyecto2.DAO.TratamientoDAO;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
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

    // --- LÓGICA DE VALIDACIÓN CON findBy ---
    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre, Long idActual) {
        // Usamos findByNombre como pediste
        Tratamiento encontrado = tratamientoDAO.findByNombre(nombre);
        
        // 1. Si es null, no existe -> No hay duplicado
        if (encontrado == null) {
            return false;
        }

        // 2. Si existe, verificamos si es el mismo registro (edición)
        if (idActual != null && encontrado.getId().equals(idActual)) {
            return false; // Soy yo mismo, no cuenta como duplicado
        }

        // 3. Si existe y tiene otro ID -> Es duplicado
        return true;
    }
}