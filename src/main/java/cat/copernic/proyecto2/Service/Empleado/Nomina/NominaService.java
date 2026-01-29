package cat.copernic.proyecto2.Service.Empleado.Nomina;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NominaService {
    List<Nomina> getAllNominas();
    // Nuevo método para procesar lógica de negocio antes de guardar
    void guardarNominaProcesada(Nomina nomina) throws Exception;
    boolean existeNominaMes(Long empleadoId, String periodo);
    Page<Nomina> listarTodasLasNominasPaginadas(String query, int page, int size);
    byte[] generarPdfNomina(Nomina nomina);
    Nomina findById(Long id);
    Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable);
    Nomina inicializarNomina(Empleado empleado);
    Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size);
    void pagarTodasLasNominas(String periodo);
    String obtenerEstadoPago(String periodo);
}