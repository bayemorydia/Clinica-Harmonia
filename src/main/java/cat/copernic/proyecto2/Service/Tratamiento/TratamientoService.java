package cat.copernic.proyecto2.Service.Tratamiento;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import java.util.List;

public interface TratamientoService {
    List<Tratamiento> listarTratamientos();
    void guardar(Tratamiento tratamiento);
    void eliminar(Tratamiento tratamiento);
    Tratamiento encontrarTratamiento(Tratamiento tratamiento);
    long countTratamientosActivos();
    boolean existeNombre(String nombre, Long idActual);
}