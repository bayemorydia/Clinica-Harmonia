package cat.copernic.proyecto2.Service.Tratamiento;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import java.util.List;

public interface TratamientoService {
    public List<Tratamiento> listarTratamientos();
    public void guardar(Tratamiento tratamiento);
    public void eliminar(Tratamiento tratamiento);
    public Tratamiento encontrarTratamiento(Tratamiento tratamiento);
    
    // Método para validar duplicados
    public boolean existeNombre(String nombre, Long idActual);
}