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
    public List<Factura> getAllFacturas() {
        return facturaDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerFacturaDetalle(Long id) throws Exception {
        return facturaDAO.findById(id)
                .orElseThrow(() -> new Exception("Factura no encontrada"));
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
            f.setActivo(false); // Anulación lógica
            facturaDAO.save(f);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Factura findFacturaById(Long id) {
        return facturaDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Factura> getFacturasPaginadas(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEmision").descending());

        Page<Factura> resultado;
        if (query == null || query.trim().isEmpty()) {
            resultado = facturaDAO.findAll(pageable);
        } else {
            resultado = facturaDAO.findByNombreOrConcepto(query, pageable);
        }

        resultado.getContent().forEach(f -> {
            if (f.getLineas() != null)
                f.getLineas().size();
        });

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalFacturas() {
        return facturaDAO.count();
    }

    @Async("fileExecutor")
    @Override
    public CompletableFuture<byte[]> generarReporteAsync(String periodo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                LocalDate fechaLimite = switch (periodo) {
                    case "DIA" -> LocalDate.now();
                    case "SEMANA" -> LocalDate.now().minusWeeks(1);
                    case "MES" -> LocalDate.now().minusMonths(1);
                    case "ANYO" -> LocalDate.now().minusYears(1);
                    default -> LocalDate.now().minusYears(100);
                };

                List<Factura> facturas = facturaDAO.findByFechaEmisionAfter(fechaLimite);
                StringBuilder csv = new StringBuilder("ID;Fecha;Paciente;Total;Estado\n");
                for (Factura f : facturas) {
                    String nombre = f.getPaciente() != null
                            ? f.getPaciente().getNombre() + " " + f.getPaciente().getApellidos()
                            : "N/A";
                    csv.append(f.getId()).append(";")
                            .append(f.getFechaEmision()).append(";")
                            .append(nombre).append(";")
                            .append(f.getTotal()).append(";")
                            .append(f.isActivo() ? "VIGENTE" : "ANULADA").append("\n");
                }
                return csv.toString().getBytes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> findByPaciente(Paciente paciente) {
        return facturaDAO.findByPaciente(paciente);
    }

    private void recalcularTotales(Factura factura) {
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;

        if (factura.getLineas() != null) {
            for (LineaFactura linea : factura.getLineas()) {
                BigDecimal precio = (linea.getPrecioUnitario() != null) ? linea.getPrecioUnitario() : BigDecimal.ZERO;
                BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
                BigDecimal subtotalLinea = precio.multiply(cantidad);
                linea.setSubtotal(subtotalLinea);
                subtotalAcumulado = subtotalAcumulado.add(subtotalLinea);
            }
        }

        factura.setSubtotal(subtotalAcumulado);
        BigDecimal iva = subtotalAcumulado.multiply(new BigDecimal("0.21"));
        factura.setIva(iva);
        factura.setTotal(subtotalAcumulado.add(iva));
    }
}