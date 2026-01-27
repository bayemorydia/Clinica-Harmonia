package cat.copernic.proyecto2.Service.Facturacion;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FacturaService {

    // Nuevo método para manejar la lógica de obtención con validación
    Factura obtenerFacturaDetalle(Long id) throws Exception;
    
    Page<Factura> getFacturasPaginadas(String query, int page, int size);
    List<Factura> findByPaciente(Paciente paciente);
}