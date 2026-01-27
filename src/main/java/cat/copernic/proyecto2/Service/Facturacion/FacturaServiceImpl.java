package cat.copernic.proyecto2.Service.Facturacion;

import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaDAO facturaDAO;


    @Override
    @Transactional(readOnly = true)
    public Factura obtenerFacturaDetalle(Long id) throws Exception {
        return facturaDAO.findById(id).orElseThrow(() -> new Exception("Factura no encontrada"));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Factura> getFacturasPaginadas(String query, int page, int size) {
        // Creamos el objeto Pageable indicando:
        // - página actual - tamaño de página - orden descendente por fecha de emisión
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEmision").descending());

        Page<Factura> resultado;
        // Si no hay query (búsqueda vacía), devolvemos todas las facturas paginadas
        if (query == null || query.trim().isEmpty()) {
            resultado = facturaDAO.findAll(pageable);
        } else {
            // Si hay query, buscamos por nombre o concepto usando paginación
            resultado = facturaDAO.findByNombreOrConcepto(query, pageable);
        }

        // Se fuerza la carga de las líneas de la factura (para evitar LazyInitializationException)
        // si las líneas existen, se llama a size() para inicializarlas
        resultado.getContent().forEach(f -> {
            if (f.getLineas() != null) f.getLineas().size();
        });

        return resultado;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Factura> findByPaciente(Paciente paciente) {
        return facturaDAO.findByPaciente(paciente);
    }


}