package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import org.springframework.data.domain.Page;

public interface CitaService {
    Page<Cita> getCitasPaginadas(String query, int page, int size);
    void saveCita(Cita cita) throws Exception;
    void anularCita(Long id);
    long countCitasActivas();
    Cita findCitaById(Long id);
}
