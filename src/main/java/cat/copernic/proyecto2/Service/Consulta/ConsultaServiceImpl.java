package cat.copernic.proyecto2.Service.Consulta;

import cat.copernic.proyecto2.DAO.JPA.*;
import cat.copernic.proyecto2.Domain.PersonalSanitario.*;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Facturacion.EstadoPago;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultaServiceImpl implements ConsultaService {

    @Autowired
    private HistorialClinicoDAO historialDAO;
    @Autowired
    private EpisodioClinicoDAO episodioDAO;
    @Autowired
    private CitaDAO citaDAO;
    @Autowired
    private MedicamentoDAO medicamentoDAO;
    @Autowired
    private TratamientoDAO tratamientoDAO;
    @Autowired
    private RecetaMedicaDAO recetaDAO;
    @Autowired
    private FacturaDAO facturaDAO;

    @Override
    @Transactional
    public void finalizarConsulta(Long citaId, EpisodioClinico episodio, Tratamiento tratamiento,
            List<PruebaMedica> pruebas, List<LineaReceta> lineas) {

        Cita cita = citaDAO.findById(citaId).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        HistorialClinico hc = historialDAO.findByPacienteId(cita.getPaciente().getId())
                .orElseGet(() -> {
                    HistorialClinico newHc = new HistorialClinico();
                    newHc.setPaciente(cita.getPaciente());
                    return historialDAO.save(newHc);
                });

        episodio.setMedico(cita.getProfesional());
        episodio.setFecha(cita.getFechaHora());
        episodio.setMotivoConsulta(cita.getMotivo());
        episodio.setHistorial(hc);

        if (pruebas != null && !pruebas.isEmpty()) {
            episodio.setPruebasAsignadas(new ArrayList<>(pruebas));
            generarFacturaPorPruebas(cita, pruebas);
        }

        EpisodioClinico episodioGuardado = episodioDAO.save(episodio);

        if (tratamiento != null) {
            tratamiento.setEpisodio(episodioGuardado);
            tratamiento.setPaciente(cita.getPaciente());
            tratamiento.setEmpleado(cita.getProfesional());
            if (tratamiento.getFechaInicio() == null)
                tratamiento.setFechaInicio(LocalDate.now());
            tratamientoDAO.save(tratamiento);
        }

        if (lineas != null && !lineas.isEmpty()) {
            RecetaMedica receta = new RecetaMedica();
            receta.setEpisodio(episodioGuardado);
            receta.setPaciente(cita.getPaciente());
            receta.setMedico(cita.getProfesional());
            List<LineaReceta> lineasPersistibles = new ArrayList<>();
            for (LineaReceta lr : lineas) {
                lr.setReceta(receta);
                lineasPersistibles.add(lr);
            }
            receta.setLineas(lineasPersistibles);
            recetaDAO.save(receta);
        }

        cita.setEstado(EstadoCita.REALIZADA);
        citaDAO.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinico obtenerOSolucionarHistorial(Long pacienteId) {
        HistorialClinico hc = historialDAO.findByPacienteId(pacienteId).orElse(null);
        if (hc != null) {
            hc.getEpisodios().size();
            for (EpisodioClinico episodio : hc.getEpisodios()) {
                if (episodio.getPruebasAsignadas() != null)
                    episodio.getPruebasAsignadas().size();
                if (episodio.getDiagnostico() != null)
                    episodio.getDiagnostico().getNombre();
                if (episodio.getMedico() != null)
                    episodio.getMedico().getNombre();
                if (episodio.getTratamientos() != null)
                    episodio.getTratamientos().size();
                if (episodio.getRecetas() != null) {
                    episodio.getRecetas().size();
                    for (RecetaMedica receta : episodio.getRecetas()) {
                        if (receta.getLineas() != null) {
                            receta.getLineas().size();
                            receta.getLineas().forEach(l -> {
                                if (l.getMedicamento() != null)
                                    l.getMedicamento().getNombre();
                            });
                        }
                    }
                }
            }
        }
        return hc;
    }

    private void recalcularTotales(Factura factura) {
        // 1. Inicializamos el subtotal acumulado
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;

        // 2. Validamos que la lista de líneas no sea nula para evitar
        // NullPointerException
        if (factura.getLineas() != null) {
            for (LineaFactura linea : factura.getLineas()) {
                // Validamos que el subtotal de la línea no sea nulo antes de sumar
                BigDecimal stLinea = (linea.getSubtotal() != null) ? linea.getSubtotal() : BigDecimal.ZERO;
                subtotalAcumulado = subtotalAcumulado.add(stLinea);
            }
        }

        // 3. Asignamos el subtotal a la factura
        factura.setSubtotal(subtotalAcumulado);

        // 4. Calculamos el IVA (21%) asegurando el redondeo para contabilidad
        // Usamos HALF_UP para redondear a 2 decimales (estándar financiero)
        BigDecimal iva = subtotalAcumulado.multiply(new BigDecimal("0.21"))
                .setScale(2, RoundingMode.HALF_UP);
        factura.setIva(iva);

        // 5. Calculamos el total final
        factura.setTotal(subtotalAcumulado.add(iva));
    }

    private void generarFacturaPorPruebas(Cita cita, List<PruebaMedica> pruebas) {
        Factura factura = new Factura();
        factura.setPaciente(cita.getPaciente());
        factura.setFechaEmision(LocalDate.now());
        factura.setFechaVencimiento(LocalDate.now().plusDays(15));
        factura.setEstado(EstadoPago.PENDIENTE);
        factura.setActivo(true);

        List<LineaFactura> lineas = new ArrayList<>();

        for (PruebaMedica p : pruebas) {
            LineaFactura lf = new LineaFactura();
            lf.setConcepto("Prueba Médica: " + p.getNombre() + " (Cita #" + cita.getId() + ")");
            lf.setCantidad(1);

            // CORRECCIÓN: Asignar BigDecimal directamente sin usar .floatValue()
            // Asumiendo que p.getPrecio() ya devuelve BigDecimal
            BigDecimal precioPrueba = (p.getPrecio() != null) ? p.getPrecio() : BigDecimal.ZERO;

            lf.setPrecioUnitario(precioPrueba);
            lf.setSubtotal(precioPrueba); // Cantidad es 1, subtotal = precio
            lf.setFactura(factura);

            lineas.add(lf);
        }

        factura.setLineas(lineas);

        // CORRECCIÓN: Usar el método centralizado para calcular Subtotal, IVA y Total
        recalcularTotales(factura);

        facturaDAO.save(factura);
    }

    @Override
    public List<Medicamento> buscarMedicamentos(String q) {
        return medicamentoDAO.findByNombreContainingIgnoreCase(q);
    }

    @Override
    @Transactional
    public void cambiarEstadoCita(Long id, EstadoCita estado) {
        Cita cita = citaDAO.findById(id).orElseThrow();
        cita.setEstado(estado);
        citaDAO.save(cita);
    }

    @Override
    @Transactional
    public void guardarAtencion(EpisodioClinico e, List<LineaReceta> l, Long c) {
        finalizarConsulta(c, e, null, null, l);
    }
}