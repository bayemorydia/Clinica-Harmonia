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
import java.util.List;
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

    // Contar citas de HOY para un médico específico (Solo las que no están
    // CANCELADAS)
    long countByProfesionalIdAndFechaHoraBetweenAndEstadoNot(
            Long profesionalId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);

    // Contar pacientes únicos que tienen citas PENDIENTES con este médico
    @Query("SELECT COUNT(DISTINCT c.paciente) FROM Cita c " +
            "WHERE c.profesional.id = :medicoId AND c.estado = :estado")
    long countPacientesConCitasPendientes(@Param("medicoId") Long medicoId, @Param("estado") EstadoCita estado);

    // Listar las citas pendientes de un médico para mostrarlas en el Dashboard
    List<Cita> findByProfesionalIdAndEstadoOrderByFechaHoraAsc(Long medicoId, EstadoCita estado);

    // Para la agenda: Citas entre dos fechas, de un médico, que NO estén ANULADAS
    List<Cita> findByProfesionalIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
        Long medicoId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);

}