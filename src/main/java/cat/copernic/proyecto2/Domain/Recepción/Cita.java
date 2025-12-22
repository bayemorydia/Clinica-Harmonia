package cat.copernic.proyecto2.Domain.Recepción;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "Cita")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull(message = "Debe seleccionar un paciente")
    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ToString.Exclude
    @NotNull(message = "Debe asignar un profesional")
    @ManyToOne
    @JoinColumn(name = "profesional_id")
    private Empleado profesional;

    @NotNull(message = "La fecha y hora es obligatoria")
    @Future(message = "La cita debe ser en una fecha futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
    @Size(max = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PROGRAMADA;
}
