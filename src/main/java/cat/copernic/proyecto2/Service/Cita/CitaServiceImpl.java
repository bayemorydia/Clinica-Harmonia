package cat.copernic.proyecto2.Service.Cita;

import cat.copernic.proyecto2.DAO.JPA.CitaDAO;
import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Facturacion.EstadoPago;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaDAO citaDAO;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private FacturaDAO facturaDAO;


    @Override
    @Transactional(readOnly = true)
    public long countCitasHoy(Long medicoId) {
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
        return citaDAO.countByProfesionalIdAndFechaHoraBetweenAndEstadoNot(medicoId, inicio, fin, EstadoCita.ANULADA);
    }

    //Este método intenta convertir una fecha en formato String a un objeto LocalDate.
//Si la cadena es nula, vacía o no tiene un formato válido, devuelve la fecha actual.
    @Override
    public LocalDate parsearFechaAgenda(String fechaStr) {
        try {
            return (fechaStr == null || fechaStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(fechaStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarMedicosActivos() {

        // Obtenemos la lista completa de empleados
        return empleadoService.listarTodos().stream()

                // Filtramos solo los empleados que tienen rol y cuyo nombre es "MEDICO"
                .filter(e -> e.getRol() != null && "MEDICO".equalsIgnoreCase(e.getRol().getNombre()))

                // Filtramos solo los empleados que están activos
                .filter(Empleado::getActiva)

                // Convertimos el stream filtrado a una lista
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Cita> getCitasPaginadas(String query, EstadoCita estado, int page, int size) {
        return citaDAO.findByFilters(query, estado, PageRequest.of(page, size, Sort.by("fechaHora").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Cita findCitaById(Long id) {
        return citaDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCitasActivas() {
        return citaDAO.countByEstadoNot(EstadoCita.ANULADA);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCita(Cita cita) throws Exception {

        // Validamos las reglas de negocio antes de guardar la cita
        validarReglasNegocio(cita);

        // Comprobamos si la cita es nueva (no tiene ID)
        boolean esNueva = (cita.getId() == null);

        // Si es nueva, establecemos el estado inicial como PROGRAMADA
        if (esNueva) {
            cita.setEstado(EstadoCita.PROGRAMADA);
        }

        // Guardamos la cita en la base de datos
        Cita guardada = citaDAO.save(cita);

        // Si la cita es nueva, generamos una factura asociada a la misma
        if (esNueva) {
            generarFacturaAsociada(guardada);
        }
    }


    /**
     * Lógica de validación extraída para mayor claridad
     */
    private void validarReglasNegocio(Cita cita) throws Exception {

        // Validación básica: la fecha y hora son obligatorias
        if (cita.getFechaHora() == null)
            throw new Exception("La fecha y hora son obligatorias.");

        // Normalizar la fecha/hora a la hora exacta (sin minutos ni segundos)
        LocalDateTime fecha = cita.getFechaHora().truncatedTo(ChronoUnit.HOURS);
        cita.setFechaHora(fecha);

        // Validación: no se puede agendar una cita en el pasado
        if (fecha.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)))
            throw new Exception("No se pueden agendar citas en el pasado.");

        // Validación del horario permitido:
        // - Mañana: 08:00 a 12:00
        // - Tarde: 14:00 a 16:00
        int hora = fecha.getHour();
        boolean horarioValido = (hora >= 8 && hora <= 12) || (hora >= 14 && hora <= 16);
        if (!horarioValido)
            throw new Exception("El horario permitido es de 08:00-12:00 y 14:00-16:00.");

        // Validación de días hábiles: solo de lunes a viernes
        if (fecha.getDayOfWeek().getValue() > 5)
            throw new Exception("Solo se pueden agendar citas de lunes a viernes.");

        // Validación de disponibilidad del paciente:
        // Si ya tiene una cita en la misma fecha/hora (y no está anulada), lanza excepción
        validarDisponibilidad(
                citaDAO.findByPacienteAndFechaHoraAndEstadoNot(cita.getPaciente(), fecha, EstadoCita.ANULADA),
                cita,
                "El paciente ya tiene una cita a esta hora."
        );

        // Validación de disponibilidad del profesional:
        // Si el profesional ya tiene una cita en la misma fecha/hora (y no está anulada), lanza excepción
        validarDisponibilidad(
                citaDAO.findByProfesionalAndFechaHoraAndEstadoNot(cita.getProfesional(), fecha, EstadoCita.ANULADA),
                cita,
                "El médico ya tiene una cita asignada a esta hora."
        );
    }

    // Método auxiliar para evitar repetir la lógica del ID
    private void validarDisponibilidad(Optional<Cita> existente, Cita actual, String mensaje) throws Exception {
        if (existente.isPresent() && !existente.get().getId().equals(actual.getId())) {
            throw new Exception(mensaje);
        }
    }

    /**
     * Lógica de facturación extraída
     */
    private void generarFacturaAsociada(Cita guardada) {
        Factura f = new Factura();
        f.setPaciente(guardada.getPaciente());
        f.setFechaEmision(LocalDate.now());
        f.setEstado(EstadoPago.PENDIENTE);
        f.setActivo(true);

        BigDecimal precioBase = new BigDecimal("50.00");

        LineaFactura lf = new LineaFactura();
        lf.setConcepto("Consulta Médica: " + guardada.getEspecialidad() + " (Cita #" + guardada.getId() + ")");
        lf.setCantidad(1);
        lf.setPrecioUnitario(precioBase);
        lf.setSubtotal(precioBase);
        lf.setFactura(f);

        f.setLineas(new ArrayList<>(List.of(lf)));
        recalcularTotales(f);
        facturaDAO.save(f);
    }

    private void recalcularTotales(Factura factura) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaFactura linea : factura.getLineas()) {
            subtotal = subtotal.add(linea.getSubtotal());
        }
        factura.setSubtotal(subtotal);
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.21"));
        factura.setIva(iva);
        factura.setTotal(subtotal.add(iva));
    }

    @Override
    @Transactional
    public void anularCita(Long id) {
        Cita cita = findCitaById(id);
        if (cita != null && cita.getEstado() != EstadoCita.ANULADA) {
            cita.setEstado(EstadoCita.ANULADA);
            citaDAO.save(cita);

            List<Factura> facturas = facturaDAO.findAll();
            for (Factura f : facturas) {
                if (f.getLineas() != null && !f.getLineas().isEmpty()) {
                    if (f.getLineas().get(0).getConcepto().contains("(Cita #" + id + ")")) {
                        f.setEstado(EstadoPago.ANULADO);
                        f.setActivo(false);
                        facturaDAO.save(f);
                        break;
                    }
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPacientesPendientesPorMedico(Long medicoId) {
        return citaDAO.countPacientesConCitasPendientes(medicoId, EstadoCita.PROGRAMADA);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasPendientesMedico(Long medicoId) {
        return citaDAO.findByProfesionalIdAndEstadoOrderByFechaHoraAsc(medicoId, EstadoCita.PROGRAMADA);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasDia(Long medicoId, LocalDate fecha) {
        return citaDAO.findByProfesionalIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, fecha.atStartOfDay(),
                fecha.atTime(23, 59, 59));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasDeHoyGlobal() {
        return listarCitasPorDiaGlobal(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorDiaGlobal(LocalDate fecha) {
        return citaDAO.findByFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(fecha.atStartOfDay(),
                fecha.atTime(23, 59, 59), EstadoCita.ANULADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> findByPaciente(Paciente paciente) {
        return citaDAO.findByPaciente(paciente);
    }
}