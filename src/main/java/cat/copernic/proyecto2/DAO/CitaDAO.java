package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CitaDAO extends JpaRepository<Cita, Long> {

    // Búsqueda paginada por nombre de paciente o especialidad
    @Query("SELECT c FROM Cita c WHERE " +
            "LOWER(c.paciente.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.paciente.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.especialidad) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Cita> findByQuery(@Param("query") String query, Pageable pageable);
    long countByEstadoNot(EstadoCita estado);
    long countByFechaHoraBetweenAndEstadoNot(LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);

    // Búsqueda para validaciones (devuelven el objeto para comparar IDs en edición)
    Optional<Cita> findByPacienteAndFechaHoraAndEstadoNot(Paciente p, LocalDateTime fh, EstadoCita e);

    Optional<Cita> findByProfesionalAndFechaHoraAndEstadoNot(Empleado m, LocalDateTime fh, EstadoCita e);

    // Validaciones de solapamiento
    boolean existsByPacienteAndFechaHoraAndEstadoNot(Paciente p, LocalDateTime fh, EstadoCita e);

    boolean existsByProfesionalAndFechaHoraAndEstadoNot(Empleado m, LocalDateTime fh, EstadoCita e);
}