package cat.copernic.proyecto2.Service;

import cat.copernic.proyecto2.Domain.Personal_sanitario.Tratamiento;
import java.util.List;

public interface TratamientoService {
    public List<Tratamiento> listarTratamientos();
    public void guardar(Tratamiento tratamiento);
    public void eliminar(Tratamiento tratamiento);
    public Tratamiento encontrarTratamiento(Tratamiento tratamiento);
}
