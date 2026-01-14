package cat.copernic.proyecto2.Service.Empleado.Nomina;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import cat.copernic.proyecto2.DAO.JPA.NominaDAO;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.util.List;

@Service
public class NominaServiceImpl implements NominaService {

    @Autowired
    private NominaDAO nominaRepository;

    public Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable) {
        return nominaRepository.findByEmpleadoId(empleadoId, pageable);
    }

    @Override
    public List<Nomina> getAllNominas() {
        return nominaRepository.findAll();
    }

    public Nomina findById(Long id) {
        return nominaRepository.findById(id).orElse(null);
    }

    @Override
    public void saveNomina(Nomina nomina) {
        nominaRepository.save(nomina);
    }

    @Override
    public void updateNomina(Long id, Nomina nomina) {
        Nomina existente = nominaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nómina no encontrada"));
        existente.setPeriodo(nomina.getPeriodo());
        existente.setSalarioBase(nomina.getSalarioBase());
        existente.setComplementos(nomina.getComplementos());
        existente.setDeducciones(nomina.getDeducciones());
        existente.setEmpleado(nomina.getEmpleado());
        nominaRepository.save(existente);
    }

    @Override
    public boolean existeNominaMes(Long empleadoId, String periodo) {
        return nominaRepository.existsByEmpleadoIdAndPeriodo(empleadoId, periodo);
    }

    @Override
    public byte[] generarPdfNomina(Nomina nomina) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Crear PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título principal
            Paragraph titulo = new Paragraph("NÓMINA MENSUAL")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold();
            document.add(titulo);

            document.add(new Paragraph(" ")); // espacio

            // Datos del empleado
            Table datosEmpleado = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth();

            datosEmpleado.addCell(new Cell().add(new Paragraph("Empleado:").setBold()).setBorder(null));
            datosEmpleado.addCell(new Cell().add(new Paragraph(nomina.getEmpleado().getNombre() + " " + nomina.getEmpleado().getApellidos())).setBorder(null));

            datosEmpleado.addCell(new Cell().add(new Paragraph("DNI/NIE:").setBold()).setBorder(null));
            datosEmpleado.addCell(new Cell().add(new Paragraph(nomina.getEmpleado().getDni())).setBorder(null));

            datosEmpleado.addCell(new Cell().add(new Paragraph("Departamento:").setBold()).setBorder(null));
            datosEmpleado.addCell(new Cell().add(new Paragraph(nomina.getEmpleado().getDepartamento())).setBorder(null));

            datosEmpleado.addCell(new Cell().add(new Paragraph("Periodo:").setBold()).setBorder(null));
            datosEmpleado.addCell(new Cell().add(new Paragraph(nomina.getPeriodo())).setBorder(null));

            document.add(datosEmpleado);

            document.add(new Paragraph(" ")); // espacio

            // Tabla de conceptos y cantidades
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                    .useAllAvailableWidth();

            // Encabezado
            tabla.addHeaderCell(new Cell().add(new Paragraph("Concepto").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Importe (€)").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));

            // Filas de conceptos
            tabla.addCell("Salario Base");
            tabla.addCell(new Paragraph(String.format("%.2f", nomina.getSalarioBase())).setTextAlignment(TextAlignment.RIGHT));

            tabla.addCell("Complementos");
            tabla.addCell(new Paragraph(String.format("%.2f", nomina.getComplementos())).setTextAlignment(TextAlignment.RIGHT));

            tabla.addCell("Deducciones");
            tabla.addCell(new Paragraph(String.format("%.2f", nomina.getDeducciones())).setTextAlignment(TextAlignment.RIGHT));

            // Total
            tabla.addCell(new Cell().add(new Paragraph("Total a Pagar").setBold()));
            tabla.addCell(new Cell().add(new Paragraph(String.format("%.2f", nomina.getTotalPagar())).setBold()).setTextAlignment(TextAlignment.RIGHT));

            document.add(tabla);

            document.add(new Paragraph(" ")); // espacio
            document.add(new Paragraph("Gracias por su dedicación.").setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
