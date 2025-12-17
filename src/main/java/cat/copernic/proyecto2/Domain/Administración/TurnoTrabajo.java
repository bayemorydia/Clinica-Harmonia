package cat.copernic.proyecto2.Domain.Administración;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="TurnoTrabajo")

public class TurnoTrabajo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String tipoTurno;


}
