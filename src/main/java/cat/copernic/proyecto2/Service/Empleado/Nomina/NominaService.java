package cat.copernic.proyecto2.Service.Empleado.Nomina;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
public interface NominaService {
    List<Nomina> getAllNominas();
    void saveNomina(Nomina nomina);
    void updateNomina(Long id, Nomina nomina);
    boolean existeNominaMes(Long empleadoId, String periodo);
    byte[] generarPdfNomina(Nomina nomina);
    Nomina findById(Long id);
    Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable);
}
