package cat.copernic.proyecto2.Domain.Administración;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "vacaciones")
public class Vacaciones {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int totalDias;

    @Enumerated(EnumType.STRING)
    private EstadoVacaciones estado = EstadoVacaciones.PENDIENTE;
}
