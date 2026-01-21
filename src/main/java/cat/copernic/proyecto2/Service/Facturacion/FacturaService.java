package cat.copernic.proyecto2.Service.Facturacion;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FacturaService {
    List<Factura> getAllFacturas();
    void saveFactura(Factura factura);
    void deleteFactura(Long id);
    Factura findFacturaById(Long id);
    Page<Factura> getFacturasPaginadas(String query, int page, int size);
    long countTotalFacturas();
    CompletableFuture<byte[]> generarReporteAsync(String periodo);
    
    // Método para Telegram
    List<Factura> findByPaciente(Paciente paciente);
}