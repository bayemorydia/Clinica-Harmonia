package cat.copernic.proyecto2.Service.Empleado.Nomina;

import cat.copernic.proyecto2.DAO.JPA.NominaDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import org.springframework.data.domain.Sort;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import java.util.concurrent.CompletableFuture;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class NominaServiceImpl implements NominaService {

    @Autowired
    private NominaDAO nominaRepository;

    @Autowired
    private EmpleadoService empleadoService;

    @Override
    @Transactional(readOnly = true)
    public Page<Nomina> getNominasPorEmpleado(Long empleadoId, Pageable pageable) {
        return nominaRepository.findByEmpleadoId(empleadoId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Nomina> getAllNominas() {
        return nominaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Nomina findById(Long id) {
        return nominaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void saveNomina(Nomina nomina) {
        this.asegurarValoresFinancieros(nomina);
        nominaRepository.save(nomina);
    }

    @Override
    @Transactional
    public void guardarNominaProcesada(Nomina nomina) throws Exception {
        Empleado empleado = empleadoService.obtenerPorId(nomina.getEmpleado().getId());
        if (empleado == null)
            throw new Exception("Empleado no encontrado");

        if (existeNominaMes(empleado.getId(), nomina.getPeriodo())) {
            throw new Exception("Ya existe una nómina para este período");
        }

        asegurarValoresFinancieros(nomina);
        nominaRepository.save(nomina);
    }

    private void asegurarValoresFinancieros(Nomina nomina) {
        if (nomina.getSalarioBase() == null)
            nomina.setSalarioBase(BigDecimal.ZERO);
        if (nomina.getDeducciones() == null)
            nomina.setDeducciones(BigDecimal.ZERO);
        if (nomina.getTotalPagar() == null) {
            nomina.setTotalPagar(nomina.getSalarioBase().subtract(nomina.getDeducciones()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNominaMes(Long empleadoId, String periodo) {
        return nominaRepository.existsByEmpleadoIdAndPeriodo(empleadoId, periodo);
    }

    @Override
    public byte[] generarPdfNomina(Nomina nomina) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph titulo = new Paragraph("NÓMINA MENSUAL", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            PdfPTable datosEmpleado = new PdfPTable(2);
            datosEmpleado.setWidthPercentage(100);
            datosEmpleado.setWidths(new float[] { 1, 2 });

            addDatoEmpleado(datosEmpleado, "Empleado:",
                    nomina.getEmpleado().getNombre() + " " + nomina.getEmpleado().getApellidos(), boldFont,
                    regularFont);
            addDatoEmpleado(datosEmpleado, "DNI/NIE:", nomina.getEmpleado().getDni(), boldFont, regularFont);
            addDatoEmpleado(datosEmpleado, "Periodo:", nomina.getPeriodo(), boldFont, regularFont);

            document.add(datosEmpleado);
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 3, 2 });

            PdfPCell header1 = new PdfPCell(new Phrase("Concepto", boldFont));
            header1.setBackgroundColor(Color.LIGHT_GRAY);
            header1.setPadding(5);
            tabla.addCell(header1);

            PdfPCell header2 = new PdfPCell(new Phrase("Importe (€)", boldFont));
            header2.setBackgroundColor(Color.LIGHT_GRAY);
            header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            header2.setPadding(5);
            tabla.addCell(header2);

            addFilaTabla(tabla, "Salario Base", nomina.getSalarioBase(), regularFont);
            addFilaTabla(tabla, "IRPF", nomina.getDeducciones(), regularFont);

            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("Neto Mensual", boldFont));
            cellTotalLabel.setPadding(5);
            tabla.addCell(cellTotalLabel);

            BigDecimal total = (nomina.getNetoMensual() != null) ? nomina.getNetoMensual() : BigDecimal.ZERO;
            PdfPCell cellTotalValue = new PdfPCell(new Phrase(String.format("%.2f €", total), boldFont));
            cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValue.setPadding(5);
            tabla.addCell(cellTotalValue);

            document.add(tabla);
            document.add(new Paragraph("\n\n"));
            Paragraph pie = new Paragraph("Gracias por su dedicación.", regularFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            document.add(pie);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void addDatoEmpleado(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);
        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellValue);
    }

    private void addFilaTabla(PdfPTable table, String concepto, BigDecimal valor, Font font) {
        PdfPCell cellConcepto = new PdfPCell(new Phrase(concepto, font));
        cellConcepto.setPadding(5);
        table.addCell(cellConcepto);
        BigDecimal valorSeguro = (valor != null) ? valor : BigDecimal.ZERO;
        PdfPCell cellValor = new PdfPCell(new Phrase(String.format("%.2f", valorSeguro), font));
        cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValor.setPadding(5);
        table.addCell(cellValor);
    }

    private void calcularNomina(Nomina nomina) {
        BigDecimal salarioBase = nomina.getSalarioBase() != null ? nomina.getSalarioBase() : BigDecimal.ZERO;
        BigDecimal irpfPorcentaje;

        if (salarioBase.compareTo(BigDecimal.valueOf(12450)) <= 0)
            irpfPorcentaje = BigDecimal.valueOf(19);
        else if (salarioBase.compareTo(BigDecimal.valueOf(20200)) <= 0)
            irpfPorcentaje = BigDecimal.valueOf(24);
        else if (salarioBase.compareTo(BigDecimal.valueOf(35200)) <= 0)
            irpfPorcentaje = BigDecimal.valueOf(30);
        else if (salarioBase.compareTo(BigDecimal.valueOf(60000)) <= 0)
            irpfPorcentaje = BigDecimal.valueOf(37);
        else
            irpfPorcentaje = BigDecimal.valueOf(45);

        BigDecimal deduccionesEuros = salarioBase.multiply(irpfPorcentaje).divide(BigDecimal.valueOf(100), 2,
                RoundingMode.HALF_UP);
        BigDecimal netoMensual = salarioBase.subtract(deduccionesEuros).divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);

        nomina.setDeducciones(irpfPorcentaje);
        nomina.setNetoMensual(netoMensual);
        nomina.setTotalPagar(salarioBase.subtract(deduccionesEuros));
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<byte[]> generarYDescargarNominasTodos(String periodo) {
        try (ByteArrayOutputStream baosZip = new ByteArrayOutputStream();
                java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(baosZip)) {

            List<Empleado> empleados = empleadoService.listarTodos();

            for (Empleado e : empleados) {
                Nomina nomina = nominaRepository.findByEmpleadoIdAndPeriodo(e.getId(), periodo);
                if (nomina == null) {
                    nomina = new Nomina();
                    nomina.setEmpleado(e);
                    nomina.setPeriodo(periodo);
                    nomina.setSalarioBase(e.getSalarioBase() != null ? e.getSalarioBase() : BigDecimal.ZERO);
                    calcularNomina(nomina);
                    nominaRepository.save(nomina);
                }

                byte[] pdf = generarPdfNomina(nomina);
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(
                        "Nomina_" + e.getNombre() + "_" + e.getApellidos() + ".pdf");
                zipOut.putNextEntry(zipEntry);
                zipOut.write(pdf);
                zipOut.closeEntry();
            }

            zipOut.finish();
            return CompletableFuture.completedFuture(baosZip.toByteArray());
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public Nomina inicializarNomina(Empleado empleado) {
        Nomina nomina = new Nomina();
        nomina.setEmpleado(empleado);
        nomina.setSalarioBase(empleado.getSalarioBase() != null ? empleado.getSalarioBase() : BigDecimal.ZERO);
        nomina.setDeducciones(BigDecimal.ZERO);
        nomina.setTotalPagar(nomina.getSalarioBase());
        return nomina;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return nominaRepository.findByEmpleadoIdAndPeriodoContaining(empleadoId, search, pageable);
    }
}