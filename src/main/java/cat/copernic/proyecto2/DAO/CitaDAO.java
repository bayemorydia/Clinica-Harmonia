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

        @Query("SELECT c FROM Cita c WHERE " +
                        "LOWER(c.paciente.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(c.paciente.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(c.especialidad) LIKE LOWER(CONCAT('%', :query, '%'))")
        Page<Cita> findByQuery(@Param("query") String query, Pageable pageable);

        long countByEstadoNot(EstadoCita estado);

        // Búsqueda para validaciones de solapamiento
        Optional<Cita> findByPacienteAndFechaHoraAndEstadoNot(Paciente p, LocalDateTime fh, EstadoCita e);

        Optional<Cita> findByProfesionalAndFechaHoraAndEstadoNot(Empleado m, LocalDateTime fh, EstadoCita e);

        long countByProfesionalIdAndFechaHoraBetweenAndEstadoNot(
                        Long profesionalId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);

        @Query("SELECT COUNT(DISTINCT c.paciente) FROM Cita c " +
                        "WHERE c.profesional.id = :medicoId AND c.estado = :estado")
        long countPacientesConCitasPendientes(@Param("medicoId") Long medicoId, @Param("estado") EstadoCita estado);

        List<Cita> findByProfesionalIdAndEstadoOrderByFechaHoraAsc(Long medicoId, EstadoCita estado);

        List<Cita> findByProfesionalIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
                        Long medicoId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);

        // Añade esto a tu CitaDAO.java
        @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND c.estado != :estado " +
                        "AND c.fechaHora > :inicio AND c.fechaHora < :fin")
        Optional<Cita> buscarCitaSolapadaPaciente(@Param("paciente") Paciente p,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        @Param("estado") EstadoCita e);

        @Query("SELECT c FROM Cita c WHERE c.profesional = :profesional AND c.estado != :estado " +
                        "AND c.fechaHora > :inicio AND c.fechaHora < :fin")
        Optional<Cita> buscarCitaSolapadaProfesional(@Param("profesional") Empleado m,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        @Param("estado") EstadoCita e);

        List<Cita> findByProfesionalIdAndFechaHoraBetweenAndEstadoOrderByFechaHoraAsc(
                        Long profesionalId,
                        LocalDateTime inicio,
                        LocalDateTime fin,
                        EstadoCita estado);
}