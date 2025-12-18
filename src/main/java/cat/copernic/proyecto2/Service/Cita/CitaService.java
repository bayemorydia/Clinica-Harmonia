package cat.copernic.proyecto2.Service.Cita;

import java.util.List;
import cat.copernic.proyecto2.Domain.Recepción.Cita;

public interface CitaService {
    List<Cita> getAllCitas();
    Cita findCitaById(Long id);
    void saveCita(Cita cita) throws Exception;
    void deleteCita(Long id);
}
