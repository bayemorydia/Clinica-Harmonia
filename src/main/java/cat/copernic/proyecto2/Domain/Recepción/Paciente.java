package cat.copernic.proyecto2.Domain.Recepción;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.PersonalSanitario.HistorialClinico;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity
@Table(name = "Paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{paciente.valid.nombre}")
    private String nombre;

    @NotBlank(message = "{paciente.valid.apellidos}")
    private String apellidos;

    @NotBlank(message = "{paciente.valid.dni_obligatorio}")
    @Pattern(regexp = "^([0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]|[XYZ][0-9]{7}[TRWAGMYFPDXBNJZSQVHLCKE])$", message = "{paciente.valid.dni_formato}")
    @Column(unique = true, nullable = false)
    private String dni;

    @Past(message = "{paciente.valid.nacimiento_futuro}")
    @NotNull(message = "{paciente.valid.nacimiento_obligatorio}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "{paciente.valid.telefono_obligatorio}")
    @Pattern(regexp = "^[0-9]{9}$", message = "{paciente.valid.telefono_formato}")
    private String telefono;

    @Column(unique = true)
    @NotBlank(message = "{paciente.valid.email_obligatorio}")
    @Email(message = "{paciente.valid.email_formato}")
    private String email;

    @NotBlank(message = "{paciente.valid.direccion_obligatorio}")
    private String direccion;

    private LocalDate fechaAlta;
    private String rutaFoto;

    @Column(name = "activo")
    private boolean activo = true;

    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "usuario_id")
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private HistorialClinico historial;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas;
}