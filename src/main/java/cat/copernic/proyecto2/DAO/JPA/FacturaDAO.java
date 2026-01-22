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

    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN f.lineas l WHERE " +
            "LOWER(f.paciente.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(f.paciente.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.concepto) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Factura> findByNombreOrConcepto(@Param("query") String query, Pageable pageable);

    List<Factura> findByFechaEmisionAfter(LocalDate fecha);

    long countByActivoTrue();

    List<Factura> findByPaciente(Paciente paciente);
}