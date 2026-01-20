package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface FacturaDAO extends JpaRepository<Factura, Long> {

    /**
     * Búsqueda paginada mejorada.
     * Ahora busca por: Tipo de factura, Nombre/Apellido del Paciente O Nombre del
     * Proveedor.
     */
    @Query("SELECT f FROM Factura f WHERE " +
            "LOWER(f.tipo) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(f.paciente.nombre) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(f.paciente.apellidos) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(f.proveedor.nombre) LIKE LOWER(concat('%', :query, '%'))")
    Page<Factura> findByNombreOrTipo(@Param("query") String query, Pageable pageable);

    /**
     * Cuenta solo las facturas que no han sido dadas de baja lógica.
     */
    long countByActivoTrue();

    /**
     * Método fundamental para el reporte asíncrono.
     * Recupera facturas desde una fecha específica hasta hoy.
     */
    List<Factura> findByFechaEmisionAfter(LocalDate fecha);

    /**
     * Opcional: Si quieres que el reporte solo incluya facturas activas
     */
    List<Factura> findByFechaEmisionAfterAndActivoTrue(LocalDate fecha);

    List<Factura> findByPaciente(Paciente paciente);
}