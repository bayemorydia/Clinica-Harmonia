package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class FacturaPdfService {

    @Autowired
    private FacturaDAO facturaDAO;

    /**
     * Genera el PDF buscando la factura vinculada a una Cita.
     */
    @Transactional(readOnly = true)
    public byte[] generarFacturaCitaPdf(Long citaId) throws Exception {
        List<Factura> facturas = facturaDAO.findAll();
        Factura factura = facturas.stream()
                .filter(f -> f.getLineas() != null && !f.getLineas().isEmpty())
                .filter(f -> f.getLineas().get(0).getConcepto().contains("(Cita #" + citaId + ")"))
                .findFirst()
                .orElseThrow(() -> new Exception("No existe una factura registrada para la cita #" + citaId));

        return generarPdf(factura);
    }

    /**
     * Genera el PDF directamente por el ID de la Factura.
     */
    @Transactional(readOnly = true)
    public byte[] generarPdfPorFacturaId(Long facturaId) throws Exception {
        Factura factura = facturaDAO.findById(facturaId)
                .orElseThrow(() -> new Exception("Factura no encontrada: " + facturaId));
        return generarPdf(factura);
    }

    /**
     * Lógica central de diseño del PDF (Estilo profesional igual a Nóminas)
     */
    private byte[] generarPdf(Factura factura) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        // Fuentes
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        // Cabecera
        Paragraph titulo = new Paragraph("FACTURA OFICIAL", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));
        // Datos del Paciente (Estructura de tabla como en Nóminas)
        PdfPTable datosPaciente = new PdfPTable(2);
        datosPaciente.setWidthPercentage(100);
        datosPaciente.setWidths(new float[] { 1, 2 });
        addCeldaDato(datosPaciente, "Nº Factura:", "#" + factura.getId(), boldFont, regularFont);
        String fecha = (factura.getFechaEmision() != null) ? factura.getFechaEmision().toString() : "N/A";
        addCeldaDato(datosPaciente, "Fecha Emisión:", fecha, boldFont, regularFont);
        if (factura.getPaciente() != null) {
            String nombre = factura.getPaciente().getNombre() + " " + factura.getPaciente().getApellidos();
            addCeldaDato(datosPaciente, "Paciente:", nombre, boldFont, regularFont);
            addCeldaDato(datosPaciente, "DNI/NIE:", factura.getPaciente().getDni(), boldFont, regularFont);
        }
        document.add(datosPaciente);
        document.add(new Paragraph(" "));
        // Tabla de Conceptos
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[] { 3, 1, 1 });
        // Encabezados
        String[] headers = { "Concepto", "Cant.", "Subtotal (€)" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(5);
            if (!h.equals("Concepto"))
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(cell);
        }
        // Líneas de factura
        if (factura.getLineas() != null) {
            for (LineaFactura linea : factura.getLineas()) {
                tabla.addCell(new Phrase(linea.getConcepto(), regularFont));
                PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(linea.getCantidad()), regularFont));
                cCant.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(cCant);
                PdfPCell cSub = new PdfPCell(new Phrase(String.format("%.2f €", linea.getSubtotal()), regularFont));
                cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(cSub);
            }
        }
        // Fila de Total
        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR", boldFont));
        totalLabel.setColspan(2);
        totalLabel.setPadding(5);
        tabla.addCell(totalLabel);
        PdfPCell totalVal = new PdfPCell(new Phrase(String.format("%.2f €", factura.getTotal()), boldFont));
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalVal.setPadding(5);
        tabla.addCell(totalVal);
        document.add(tabla);
        // Pie de página
        document.add(new Paragraph("\n\n"));
        Paragraph pie = new Paragraph("Gracias por confiar en Clínica Harmonia.", regularFont);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);
        document.close();
        return out.toByteArray();
    }

    private void addCeldaDato(PdfPTable table, String label, String value, Font lF, Font vF) {
        PdfPCell cL = new PdfPCell(new Phrase(label, lF));
        cL.setBorder(Rectangle.NO_BORDER);
        cL.setPadding(3);
        table.addCell(cL);
        PdfPCell cV = new PdfPCell(new Phrase(value, vF));
        cV.setBorder(Rectangle.NO_BORDER);
        cV.setPadding(3);
        table.addCell(cV);
    }
}