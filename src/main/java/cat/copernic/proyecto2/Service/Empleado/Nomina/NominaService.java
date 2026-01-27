package cat.copernic.proyecto2.Service.Empleado.Nomina;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NominaService {
    List<Nomina> getAllNominas();
    // Nuevo método para procesar lógica de negocio antes de guardar
    void guardarNominaProcesada(Nomina nomina) throws Exception;
    boolean existeNominaMes(Long empleadoId, String periodo);
    byte[] generarPdfNomina(Nomina nomina);
    Nomina findById(Long id);
    Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable);
    CompletableFuture<byte[]> generarYDescargarNominasTodos(String periodo);
    Nomina inicializarNomina(Empleado empleado);
    Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size);
}