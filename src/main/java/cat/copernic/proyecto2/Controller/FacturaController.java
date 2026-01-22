package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Service.Cita.FacturaPdfService;
import cat.copernic.proyecto2.Service.Facturacion.FacturaService;
import cat.copernic.proyecto2.Service.Traduccion.TraduccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/admin/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private FacturaPdfService facturaPdfService;

    @Autowired
    private TraduccionService traduccionService;

    @GetMapping("")
    public String listarFacturas(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Factura> facturaPage = facturaService.getFacturasPaginadas(query, page, size);
        model.addAttribute("facturas", facturaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", facturaPage.getTotalPages());
        model.addAttribute("totalElements", facturaPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("query", query);
        model.addAttribute("trad", traduccionService);
        return "Facturacion/listaFacturas";
    }

    @GetMapping("/descargar-pdf/{id}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable("id") Long id) {
        // 1. Validación inicial del ID
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 2. Verificar si la factura existe antes de llamar al servicio de PDF
            Factura factura = facturaService.findFacturaById(id);
            if (factura == null) {
                System.err.println("Error: No se encontró la factura con ID " + id);
                return ResponseEntity.notFound().build();
            }

            // 3. Intento de generación del PDF
            byte[] pdfBytes = facturaPdfService.generarPdfPorFacturaId(id);

            // 4. Configuración de cabeceras
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            // Usamos inline para que el navegador intente mostrar el error si el PDF está corrupto
            // o attachment para forzar descarga. Cambia a .inline() si quieres ver el error en el navegador.
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Factura_" + id + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } catch (Exception e) {
            // ESTO ES LO MÁS IMPORTANTE: Mira la consola de tu terminal/IDE
            System.err.println("======= ERROR CRÍTICO GENERANDO PDF =======");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace(); 
            System.err.println("===========================================");
            
            // Devolvemos el error detallado en el cuerpo para depuración (solo en desarrollo)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleFactura(@PathVariable("id") Long id, Model model) {
        Factura factura = facturaService.findFacturaById(id);
        if (factura == null)
            return "redirect:/admin/facturas";
        model.addAttribute("factura", factura);
        model.addAttribute("trad", traduccionService);
        return "Facturacion/detalleFactura";
    }

    @GetMapping("/exportar")
    @ResponseBody
    public CompletableFuture<ResponseEntity<byte[]>> exportar(@RequestParam String periodo) {
        return facturaService.generarReporteAsync(periodo)
                .thenApply(bytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDisposition(ContentDisposition.attachment()
                            .filename("Reporte_" + periodo + ".csv").build());
                    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
                });
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Factura> facturaPage = facturaService.getFacturasPaginadas(query, page, size);
        model.addAttribute("facturas", facturaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", facturaPage.getTotalPages());
        model.addAttribute("totalElements", facturaPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trad", traduccionService);
        return "Facturacion/listaFacturas :: tablaFacturas";
    }
}