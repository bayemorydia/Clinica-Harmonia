package cat.copernic.proyecto2.Service.Facturacion;

import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaDAO facturaDAO;

    @Override
    public List<Factura> getAllFacturas() {
        return facturaDAO.findAll();
    }

    @Override
    @Transactional
    public void saveFactura(Factura factura) {
        if (factura.getId() == null || factura.getId() == 0) {
            factura.setFechaEmision(LocalDate.now());
            factura.setActivo(true);
        } else {
            Factura existente = facturaDAO.findById(factura.getId()).orElse(null);
            if (existente != null) {
                factura.setFechaEmision(existente.getFechaEmision());
                factura.setActivo(existente.isActivo());
            }
        }
        if (factura.getLineas() != null) {
            recalcularTotales(factura);
        }
        facturaDAO.save(factura);
    }

    @Override
    @Transactional
    public void deleteFactura(Long id) {
        Factura f = facturaDAO.findById(id).orElse(null);
        if (f != null) {
            f.setActivo(false);
            facturaDAO.save(f);
        }
    }

    @Override
    public Factura findFacturaById(Long id) {
        return facturaDAO.findById(id).orElse(null);
    }

    @Override
    public Page<Factura> getFacturasPaginadas(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEmision").descending());
        if (query == null || query.isEmpty()) {
            return facturaDAO.findAll(pageable);
        }
        return facturaDAO.findByNombreOrTipo(query, pageable);
    }

    @Override
    public long countTotalFacturas() {
        return facturaDAO.countByActivoTrue();
    }

    @Async("fileExecutor")
    @Override
    public CompletableFuture<byte[]> generarReporteAsync(String periodo) {
        try {
            // Simulamos carga de trabajo pesada para el Thread
            Thread.sleep(4000);

            LocalDate fechaLimite = switch (periodo) {
                case "DIA" -> LocalDate.now();
                case "SEMANA" -> LocalDate.now().minusWeeks(1);
                case "MES" -> LocalDate.now().minusMonths(1);
                case "ANYO" -> LocalDate.now().minusYears(1);
                default -> LocalDate.now().minusYears(100);
            };

            List<Factura> facturas = facturaDAO.findByFechaEmisionAfter(fechaLimite);

            StringBuilder csv = new StringBuilder("ID;Fecha;Cliente/Proveedor;Tipo;Total\n");
            for (Factura f : facturas) {
                String ente = f.getPaciente().getNombre();
                csv.append(f.getId()).append(";")
                        .append(f.getFechaEmision()).append(";")
                        .append(ente).append(";")
                        .append(f.getTotal()).append("€\n");
            }
            return CompletableFuture.completedFuture(csv.toString().getBytes());
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private void recalcularTotales(Factura factura) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaFactura linea : factura.getLineas()) {
            BigDecimal precio = BigDecimal.valueOf(linea.getPrecioUnitario());
            BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
            BigDecimal st = precio.multiply(cantidad);
            linea.setSubtotal(st.floatValue());
            subtotal = subtotal.add(st);
        }
        factura.setSubtotal(subtotal);
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.21"));
        factura.setIva(iva);
        factura.setTotal(subtotal.add(iva));
    }
}