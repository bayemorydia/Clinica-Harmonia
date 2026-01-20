package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    public void exportar(HttpServletResponse response, Cita cita) throws IOException {
        // 1. Configuración del documento
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 2. Fuentes
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

        // 3. Título
        Paragraph titulo = new Paragraph("Factura de Cita Médica", fuenteTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("\n")); // Espacio

        // 4. Datos del Cliente y Cita
        document.add(new Paragraph("ID Factura: " + "FACT-" + cita.getId(), fuenteTexto));
        document.add(new Paragraph("Fecha de emisión: " + java.time.LocalDate.now(), fuenteTexto));
        document.add(new Paragraph("\n"));

        // 5. Tabla de detalles
        PdfPTable tabla = new PdfPTable(2); // 2 columnas
        tabla.setWidthPercentage(100f);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[] { 3.0f, 4.0f }); // Anchos relativos

        // Cabecera de la tabla
        PdfPCell celdaHeader = new PdfPCell();
        celdaHeader.setBackgroundColor(Color.LIGHT_GRAY);
        celdaHeader.setPadding(5);

        Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontCabecera.setColor(Color.WHITE);

        celdaHeader.setPhrase(new Phrase("Concepto", fontCabecera));
        tabla.addCell(celdaHeader);

        celdaHeader.setPhrase(new Phrase("Detalle", fontCabecera));
        tabla.addCell(celdaHeader);

        // Filas de datos
        tabla.addCell("Paciente");
        tabla.addCell(cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellidos());

        tabla.addCell("DNI");
        tabla.addCell(cita.getPaciente().getDni());

        tabla.addCell("Médico / Profesional");
        if (cita.getProfesional() != null) {
            tabla.addCell(cita.getProfesional().getNombre() + " " + cita.getProfesional().getApellidos());
        } else {
            tabla.addCell("No asignado");
        }

        tabla.addCell("Fecha y Hora Cita");
        tabla.addCell(cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        tabla.addCell("Especialidad");
        tabla.addCell(cita.getEspecialidad().toString());

        // Precio (Si no tienes precio en Cita, pon uno fijo o añádelo a la entidad)
        tabla.addCell("Total a Pagar");
        tabla.addCell("50.00 €"); // Ejemplo estático

        document.add(tabla);

        // 6. Pie de página
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Gracias por confiar en nuestro centro médico.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }
}
