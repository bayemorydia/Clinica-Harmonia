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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NominaServiceImpl implements NominaService {

    private final ConcurrentHashMap<String, String> estadosHilos = new ConcurrentHashMap<>();

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
    public Page<Nomina> listarTodasLasNominasPaginadas(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (query == null || query.trim().isEmpty()) {
            return nominaRepository.findAll(pageable);
        }
        return nominaRepository.findByGlobalQuery(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Nomina findById(Long id) {
        return nominaRepository.findById(id).orElse(null);
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
        // Creamos un ByteArrayOutputStream para almacenar el PDF en memoria
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Creamos el documento PDF con tamaño A4
            Document document = new Document(PageSize.A4);
            // Asociamos el documento al stream para poder escribir en él
            PdfWriter.getInstance(document, baos);
            // Abrimos el documento para poder añadir contenido
            document.open();
            // Definimos las fuentes que usaremos en el PDF
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            // Creamos el título centrado
            Paragraph titulo = new Paragraph("NÓMINA MENSUAL", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            // Añadimos un espacio en blanco
            document.add(new Paragraph(" "));
            // Creamos una tabla para los datos del empleado (2 columnas)
            PdfPTable datosEmpleado = new PdfPTable(2);
            datosEmpleado.setWidthPercentage(100);
            datosEmpleado.setWidths(new float[] { 1, 2 });
            // Añadimos filas con los datos del empleado usando el método addDatoEmpleado
            addDatoEmpleado(datosEmpleado, "Empleado:",
                    nomina.getEmpleado().getNombre() + " " + nomina.getEmpleado().getApellidos(), boldFont,
                    regularFont);
            addDatoEmpleado(datosEmpleado, "DNI/NIE:", nomina.getEmpleado().getDni(), boldFont, regularFont);
            addDatoEmpleado(datosEmpleado, "Periodo:", nomina.getPeriodo(), boldFont, regularFont);
            // Añadimos la tabla de datos del empleado al documento
            document.add(datosEmpleado);
            // Añadimos un espacio en blanco
            document.add(new Paragraph(" "));
            // Creamos una tabla para los conceptos e importes (2 columnas)
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 3, 2 });
            // Encabezado de la columna "Concepto"
            PdfPCell header1 = new PdfPCell(new Phrase("Concepto", boldFont));
            header1.setBackgroundColor(Color.LIGHT_GRAY);
            header1.setPadding(5);
            tabla.addCell(header1);
            // Encabezado de la columna "Importe (€)"
            PdfPCell header2 = new PdfPCell(new Phrase("Importe (€)", boldFont));
            header2.setBackgroundColor(Color.LIGHT_GRAY);
            header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            header2.setPadding(5);
            tabla.addCell(header2);
            // Añadimos las filas con los valores de salario base e IRPF
            addFilaTabla(tabla, "Salario Base", nomina.getSalarioBase(), regularFont);
            addFilaTabla(tabla, "IRPF", nomina.getDeducciones(), regularFont);
            // Fila final: Neto Mensual
            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("Neto Mensual", boldFont));
            cellTotalLabel.setPadding(5);
            tabla.addCell(cellTotalLabel);
            // Si el neto mensual es nulo, se usa 0
            BigDecimal total = (nomina.getNetoMensual() != null) ? nomina.getNetoMensual() : BigDecimal.ZERO;
            // Celda con el valor del neto mensual alineado a la derecha
            PdfPCell cellTotalValue = new PdfPCell(new Phrase(String.format("%.2f €", total), boldFont));
            cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValue.setPadding(5);
            tabla.addCell(cellTotalValue);
            // Añadimos la tabla al documento
            document.add(tabla);
            // Añadimos un espacio extra
            document.add(new Paragraph("\n\n"));
            // Pie de página centrado
            Paragraph pie = new Paragraph("Gracias por su dedicación.", regularFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            document.add(pie);
            // Cerramos el documento
            document.close();
            // Devolvemos el PDF como array de bytes
            return baos.toByteArray();
        } catch (Exception e) {
            // Si ocurre un error, devolvemos null
            return null;
        }
    }

    // Método para añadir una fila de datos del empleado (etiqueta + valor)
    private void addDatoEmpleado(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        // Celda con la etiqueta (ej. "Nombre:")
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER); // Sin borde para que quede limpio
        table.addCell(cellLabel);
        // Celda con el valor (ej. "Juan Pérez")
        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setBorder(Rectangle.NO_BORDER); // Sin borde para que quede limpio
        table.addCell(cellValue);
    }

    // Método para añadir una fila en la tabla de conceptos (concepto + valor)
    private void addFilaTabla(PdfPTable table, String concepto, BigDecimal valor, Font font) {
        // Celda con el nombre del concepto (ej. "Salario base")
        PdfPCell cellConcepto = new PdfPCell(new Phrase(concepto, font));
        cellConcepto.setPadding(5); // Espacio interno para que no quede pegado
        table.addCell(cellConcepto);
        // Si el valor es nulo, se usa 0 para evitar errores
        BigDecimal valorSeguro = (valor != null) ? valor : BigDecimal.ZERO;
        // Celda con el valor formateado a 2 decimales
        PdfPCell cellValor = new PdfPCell(new Phrase(String.format("%.2f", valorSeguro), font));
        cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alineación a la derecha
        cellValor.setPadding(5); // Espacio interno para que no quede pegado
        table.addCell(cellValor);
    }

    private void calcularNomina(Nomina nomina) {
        // Si el salario base es nulo, se considera 0
        BigDecimal salarioBase = nomina.getSalarioBase() != null ? nomina.getSalarioBase() : BigDecimal.ZERO;
        BigDecimal irpfPorcentaje;
        // Determinamos el porcentaje de IRPF según tramos
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
        // Calculamos las deducciones en euros (salarioBase * porcentaje / 100)
        BigDecimal deduccionesEuros = salarioBase.multiply(irpfPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        // Calculamos el neto mensual: (salarioBase - deducciones) / 12
        BigDecimal netoMensual = salarioBase.subtract(deduccionesEuros)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        nomina.setDeducciones(irpfPorcentaje);
        nomina.setNetoMensual(netoMensual);
        nomina.setTotalPagar(salarioBase.subtract(deduccionesEuros));
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

    @Override
    public void pagarTodasLasNominas(String periodo) {
        estadosHilos.put(periodo, "PROCESANDO");
        Thread hiloPago = new Thread(() -> {
            try {
                // 1. Obtenemos todos los empleados
                List<Empleado> empleados = empleadoService.listarTodos();
                for (Empleado e : empleados) {
                    // 2. Filtramos por tu atributo 'activa'
                    if (e.getActiva() != null && e.getActiva()) {
                        // 3. Verificamos si ya existe la nómina para evitar duplicados
                        if (!nominaRepository.existsByEmpleadoIdAndPeriodo(e.getId(), periodo)) {
                            Nomina nuevaNomina = new Nomina();
                            nuevaNomina.setEmpleado(e);
                            nuevaNomina.setPeriodo(periodo);
                            nuevaNomina
                                    .setSalarioBase(e.getSalarioBase() != null ? e.getSalarioBase() : BigDecimal.ZERO);
                            calcularNomina(nuevaNomina);
                            nominaRepository.save(nuevaNomina);
                        }
                    }
                    // Simulación de carga para ver el estado "Procesando..." en la web
                    Thread.sleep(500);
                }
                // 4. IMPORTANTE: Marcamos como PAGADO al finalizar el bucle
                estadosHilos.put(periodo, "PAGADO");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                estadosHilos.put(periodo, "ERROR");
            }
        });
        hiloPago.setName("Hilo-Pago-Manual-" + periodo);
        hiloPago.start();
    }

    @Override
    public String obtenerEstadoPago(String periodo) {
        // Si hay datos en BD para ese periodo, siempre devolver "PAGADO"
        if (nominaRepository.countByPeriodo(periodo) > 0)
            return "PAGADO";
        return estadosHilos.getOrDefault(periodo, "PENDIENTE");
    }
}