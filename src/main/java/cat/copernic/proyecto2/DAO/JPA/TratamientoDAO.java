package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoDAO extends JpaRepository<Tratamiento, Long> {

    /**
     * Obtiene todos los tratamientos de un paciente específico.
     * Útil para que el paciente consulte su historial.
     */
    List<Tratamiento> findByPacienteId(Long pacienteId);

    /**
     * Obtiene los tratamientos ordenados por fecha de inicio descendente.
     */
    List<Tratamiento> findByPacienteIdOrderByFechaInicioDesc(Long pacienteId);

    /**
     * Busca tratamientos que todavía están vigentes (fecha actual antes de fechaFin).
     */
    @Query("SELECT t FROM Tratamiento t WHERE t.paciente.id = :pacienteId AND t.fechaFin >= CURRENT_DATE")
    List<Tratamiento> findTratamientosVigentes(@Param("pacienteId") Long pacienteId);

    /**
     * Obtiene los tratamientos asociados a un episodio clínico concreto.
     */
    List<Tratamiento> findByEpisodioId(Long episodioId);
    
    /**
     * Obtiene tratamientos recetados por un médico específico.
     */
    List<Tratamiento> findByEmpleadoId(Long empleadoId);
}