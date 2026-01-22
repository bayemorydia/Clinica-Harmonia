package cat.copernic.proyecto2.Domain.Recepción;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "Cita")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Exclude
    @NotNull(message = "Debe seleccionar un paciente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ToString.Exclude
    @NotNull(message = "Debe asignar un profesional")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profesional_id")
    private Empleado profesional;

    @NotNull(message = "La fecha y hora es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @NotNull(message = "La especialidad es obligatoria")
    private Especialidad especialidad;

    @Size(max = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PROGRAMADA;
}