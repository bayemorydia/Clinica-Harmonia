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
            @RequestParam(value = "query", defaultValue = "") String query,
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
        try {
            byte[] pdfBytes = facturaPdfService.generarPdfPorFacturaId(id);
            // La lógica de construcción de cabeceras se mantiene aquí por ser concerniente a HTTP,
            // pero el contenido viene validado del servicio.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Factura_" + id + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleFactura(@PathVariable("id") Long id, Model model) {
        try {
            // El servicio ahora se encarga de lanzar excepción si no existe
            Factura factura = facturaService.obtenerFacturaDetalle(id);
            model.addAttribute("factura", factura);
            model.addAttribute("trad", traduccionService);
            return "Facturacion/detalleFactura";
        } catch (Exception e) {
            return "redirect:/admin/facturas";
        }
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