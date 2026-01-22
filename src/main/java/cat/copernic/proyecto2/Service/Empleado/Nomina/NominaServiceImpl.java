package cat.copernic.proyecto2.Service.Empleado.Nomina;

import cat.copernic.proyecto2.DAO.JPA.NominaDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import java.util.concurrent.CompletableFuture;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class NominaServiceImpl implements NominaService {

    @Autowired
    private NominaDAO nominaRepository;

    @Autowired
    private EmpleadoService empleadoService;

    @Override
    public Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable) {
        return nominaRepository.findByEmpleadoId(empleadoId, pageable);
    }

    @Override
    public List<Nomina> getAllNominas() {
        return nominaRepository.findAll();
    }

    @Override
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

            // 1. Configurar documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // 2. Título
            Paragraph titulo = new Paragraph("NÓMINA MENSUAL", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph(" "));

            // 3. Datos del Empleado
            PdfPTable datosEmpleado = new PdfPTable(2);
            datosEmpleado.setWidthPercentage(100);
            datosEmpleado.setWidths(new float[]{1, 2});

            addDatoEmpleado(datosEmpleado, "Empleado:", nomina.getEmpleado().getNombre() + " " + nomina.getEmpleado().getApellidos(), boldFont, regularFont);
            addDatoEmpleado(datosEmpleado, "DNI/NIE:", nomina.getEmpleado().getDni(), boldFont, regularFont);

            addDatoEmpleado(datosEmpleado, "Periodo:", nomina.getPeriodo(), boldFont, regularFont);

            document.add(datosEmpleado);
            document.add(new Paragraph(" "));

            // 4. Tabla de Conceptos
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3, 2});

            // Encabezados
            PdfPCell header1 = new PdfPCell(new Phrase("Concepto", boldFont));
            header1.setBackgroundColor(Color.LIGHT_GRAY);
            header1.setPadding(5);
            tabla.addCell(header1);

            PdfPCell header2 = new PdfPCell(new Phrase("Importe (€)", boldFont));
            header2.setBackgroundColor(Color.LIGHT_GRAY);
            header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            header2.setPadding(5);
            tabla.addCell(header2);

            // Filas (Aquí usamos BigDecimal)
            addFilaTabla(tabla, "Salario Base", nomina.getSalarioBase(), regularFont);
            addFilaTabla(tabla, "IRPF", nomina.getDeducciones(), regularFont);

            // Total
            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("Neto Mensual", boldFont));
            cellTotalLabel.setPadding(5);
            tabla.addCell(cellTotalLabel);

            // Controlamos nulos en el total por seguridad
            BigDecimal total = (nomina.getNetoMensual() != null) ? nomina.getNetoMensual() : BigDecimal.ZERO;
            PdfPCell cellTotalValue = new PdfPCell(new Phrase(String.format("%.2f €", total), boldFont));
            cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValue.setPadding(5);
            tabla.addCell(cellTotalValue);

            document.add(tabla);

            // Pie
            document.add(new Paragraph("\n\n"));
            Paragraph pie = new Paragraph("Gracias por su dedicación.", regularFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            document.add(pie);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void addDatoEmpleado(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellValue);
    }

    // CORREGIDO: Ahora acepta BigDecimal en lugar de Double
    private void addFilaTabla(PdfPTable table, String concepto, BigDecimal valor, Font font) {
        PdfPCell cellConcepto = new PdfPCell(new Phrase(concepto, font));
        cellConcepto.setPadding(5);
        table.addCell(cellConcepto);

        // Seguridad contra nulos
        BigDecimal valorSeguro = (valor != null) ? valor : BigDecimal.ZERO;

        PdfPCell cellValor = new PdfPCell(new Phrase(String.format("%.2f", valorSeguro), font));
        cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValor.setPadding(5);
        table.addCell(cellValor);
    }
    private void calcularNomina(Nomina nomina) {
        BigDecimal salarioBase = nomina.getSalarioBase() != null ? nomina.getSalarioBase() : BigDecimal.ZERO;
        BigDecimal irpfPorcentaje;

        if (salarioBase.compareTo(BigDecimal.valueOf(12450)) <= 0) irpfPorcentaje = BigDecimal.valueOf(19);
        else if (salarioBase.compareTo(BigDecimal.valueOf(20200)) <= 0) irpfPorcentaje = BigDecimal.valueOf(24);
        else if (salarioBase.compareTo(BigDecimal.valueOf(35200)) <= 0) irpfPorcentaje = BigDecimal.valueOf(30);
        else if (salarioBase.compareTo(BigDecimal.valueOf(60000)) <= 0) irpfPorcentaje = BigDecimal.valueOf(37);
        else irpfPorcentaje = BigDecimal.valueOf(45);

// Calculamos la cantidad en euros
        BigDecimal deduccionesEuros = salarioBase.multiply(irpfPorcentaje).divide(BigDecimal.valueOf(100));
        BigDecimal netoMensual = salarioBase.subtract(deduccionesEuros).divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);

// Guardamos en la nómina
        nomina.setDeducciones(irpfPorcentaje);          // cantidad en euros
        nomina.setNetoMensual(netoMensual);
        nomina.setTotalPagar(salarioBase.subtract(deduccionesEuros));
    }

    @Async
    public CompletableFuture<byte[]> generarYDescargarNominasTodos(String periodo) {
        try (ByteArrayOutputStream baosZip = new ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(baosZip)) {

            List<Empleado> empleados = empleadoService.listarTodos();

            for (Empleado e : empleados) {
                // 1. Crear nómina si no existe
                Nomina nomina;
                if (!existeNominaMes(e.getId(), periodo)) {
                    nomina = new Nomina();
                    nomina.setEmpleado(e);
                    nomina.setPeriodo(periodo);
                    nomina.setSalarioBase(e.getSalarioBase() != null ? e.getSalarioBase() : BigDecimal.ZERO);
                    nomina.setDeducciones(BigDecimal.ZERO);
                    calcularNomina(nomina);
                    nomina.setTotalPagar(nomina.getSalarioBase().subtract(nomina.getDeducciones()));
                    saveNomina(nomina);
                } else {
                    // Si ya existe, la obtenemos
                    nomina = nominaRepository.findByEmpleadoIdAndPeriodo(e.getId(), periodo);
                }

                // 2. Generar PDF
                byte[] pdf = generarPdfNomina(nomina);

                // 3. Agregar PDF al ZIP
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(
                        "Nomina_" + e.getNombre() + "_" + e.getApellidos() + ".pdf");
                zipOut.putNextEntry(zipEntry);
                zipOut.write(pdf);
                zipOut.closeEntry();
            }

            zipOut.finish();
            return CompletableFuture.completedFuture(baosZip.toByteArray());

        } catch (Exception ex) {
            ex.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

}