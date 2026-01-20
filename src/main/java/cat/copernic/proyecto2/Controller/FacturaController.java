package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
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
    public String search(@RequestParam(value = "query", required = false) String query,
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