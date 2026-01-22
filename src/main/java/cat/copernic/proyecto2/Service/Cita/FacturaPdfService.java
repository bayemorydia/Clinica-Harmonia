package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FacturaPdfService {

    @Autowired
    private FacturaDAO facturaDAO;

    /**
     * Método para el controlador que exportar directamente al flujo de respuesta.
     */
    public void exportar(HttpServletResponse response, Cita cita) throws IOException {
        try {
            byte[] pdfBytes = generarFacturaCitaPdf(cita.getId());
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar el PDF: " + e.getMessage());
        }
    }

    /**
     * Genera el PDF usando la librería OpenPDF buscando la factura real en la BD.
     */
    public byte[] generarFacturaCitaPdf(Long citaId) throws Exception {
        // Buscamos la factura en la base de datos vinculada a la cita
        List<Factura> facturas = facturaDAO.findAll();
        Factura factura = null;
        for (Factura f : facturas) {
            if (f.getLineas() != null && !f.getLineas().isEmpty()) {
                if (f.getLineas().get(0).getConcepto().contains("(Cita #" + citaId + ")")) {
                    factura = f;
                    break;
                }
            }
        }

        if (factura == null) {
            throw new Exception("No existe una factura registrada en la base de datos para la cita #" + citaId);
        }

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Estilos de fuente (OpenPDF usa java.awt.Color)
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Título
        Paragraph title = new Paragraph("CLÍNICA HARMONIA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Espacio

        // Información de la Factura
        document.add(new Paragraph("Número de Factura: " + factura.getId(), boldFont));
        document.add(new Paragraph("Fecha de Emisión: " + factura.getFechaEmision(), normalFont));
        String estadoTexto = factura.isActivo() ? "VIGENTE" : "ANULADA";
        document.add(new Paragraph("Estado: " + estadoTexto, boldFont));
        document.add(new Paragraph(" "));

        // Información del Paciente
        document.add(new Paragraph("DATOS DEL PACIENTE:", boldFont));
        document.add(new Paragraph(
                "Nombre: " + factura.getPaciente().getNombre() + " " + factura.getPaciente().getApellidos(),
                normalFont));
        document.add(new Paragraph("DNI: " + factura.getPaciente().getDni(), normalFont));
        document.add(new Paragraph(" "));

        // Tabla de productos/servicios
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        // Cabeceras de tabla
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setPadding(5);

        cell.setPhrase(new Phrase("Concepto", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Cantidad", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Subtotal", boldFont));
        table.addCell(cell);

        // Filas de la factura
        for (LineaFactura linea : factura.getLineas()) {
            table.addCell(new Phrase(linea.getConcepto(), normalFont));
            table.addCell(new Phrase(String.valueOf(linea.getCantidad()), normalFont));
            table.addCell(new Phrase(linea.getSubtotal() + " €", normalFont));
        }
        document.add(table);

        // Totales
        document.add(new Paragraph(" "));
        Paragraph totales = new Paragraph();
        totales.add(new Chunk("Subtotal: " + factura.getSubtotal() + " €\n", normalFont));
        totales.add(new Chunk("IVA (21%): " + factura.getIva() + " €\n", normalFont));
        totales.add(new Chunk("TOTAL A PAGAR: " + factura.getTotal() + " €", titleFont));
        totales.setAlignment(Element.ALIGN_RIGHT);
        document.add(totales);

        document.close();
        return out.toByteArray();
    }

    public void exportarFacturaDirecta(HttpServletResponse response, Long facturaId) throws IOException {
        try {
            byte[] pdfBytes = generarPdfPorFacturaId(facturaId);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    public byte[] generarPdfPorFacturaId(Long facturaId) throws Exception {
        Factura factura = facturaDAO.findById(facturaId)
                .orElseThrow(() -> new Exception("Factura no encontrada: " + facturaId));

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Fuentes
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

        // ... (Cabecera igual)

        // 4. Información de Factura (Asegurar conversión a String)
        document.add(new Paragraph("FACTURA #" + factura.getId(), boldFont));
        String fecha = (factura.getFechaEmision() != null) ? factura.getFechaEmision().toString() : "N/A";
        document.add(new Paragraph("Fecha: " + fecha, normalFont));

        // Validación de Paciente
        if (factura.getPaciente() != null) {
            String nombre = (factura.getPaciente().getNombre() != null) ? factura.getPaciente().getNombre() : "";
            String apellidos = (factura.getPaciente().getApellidos() != null) ? factura.getPaciente().getApellidos()
                    : "";
            document.add(new Paragraph("Paciente: " + nombre + " " + apellidos, normalFont));
        }
        document.add(new Paragraph(" "));

        // 5. Tabla de conceptos
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3f, 1f, 1f });

        table.addCell(new Phrase("CONCEPTO", boldFont));
        table.addCell(new Phrase("CANT.", boldFont));
        table.addCell(new Phrase("SUBTOTAL", boldFont));

        if (factura.getLineas() != null) {
            for (LineaFactura linea : factura.getLineas()) {
                table.addCell(new Phrase(linea.getConcepto() != null ? linea.getConcepto() : "-", normalFont));
                table.addCell(new Phrase(String.valueOf(linea.getCantidad()), normalFont));
                // Convertir float a String explícitamente
                table.addCell(new Phrase(String.format("%.2f", linea.getSubtotal()) + " €", normalFont));
            }
        }
        document.add(table);

        // 6. Totales (Convertir BigDecimal a String de forma segura)
        document.add(new Paragraph(" "));
        Paragraph totales = new Paragraph();
        totales.setAlignment(Element.ALIGN_RIGHT);

        String totalTexto = (factura.getTotal() != null) ? factura.getTotal().toString() : "0.00";
        totales.add(new Chunk("TOTAL: " + totalTexto + " €", titleFont));
        document.add(totales);

        document.close();
        return out.toByteArray();
    }
}