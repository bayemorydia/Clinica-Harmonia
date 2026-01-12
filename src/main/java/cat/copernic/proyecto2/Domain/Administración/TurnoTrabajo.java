package cat.copernic.proyecto2.Domain.Administración;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TurnoTrabajo")
public class TurnoTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El empleado es obligatorio")
    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDateTime inicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fin;

    @NotBlank(message = "El tipo de turno es obligatorio")
    private String tipoTurno;
}
