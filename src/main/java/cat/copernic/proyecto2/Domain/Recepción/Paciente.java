package cat.copernic.proyecto2.Domain.Recepción;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.PersonalSanitario.HistorialClinico;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode; // <--- IMPORTANTE: AÑADIR ESTE IMPORT
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
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
    @Pattern(regexp = "^([0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]|[XYZ][0-9]{7}[TRWAGMYFPDXBNJZSQVHLCKE])$", message = "Formato de DNI o NIE no válido")
    @Column(unique = true, nullable = false)
    private String dni;
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    @Column(unique = true)
    private String telefono;
    @Email(message = "Formato de email incorrecto")
    private String email;
    @Pattern(regexp = "^[0-9]{12}$", message = "El NSS debe tener exactamente 12 dígitos")
    @Column(unique = true)
    private String numeroSeguridadSocial;
    private String direccion;
    private LocalDate fechaAlta;
    private String rutaFoto; 
    @Column(name = "activo")
    private boolean activo = true;

    // Relación con login
    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "usuario_id")
    @EqualsAndHashCode.Exclude // <--- AÑADIR ESTO (Rompe el bucle con Usuario)
    private Usuario usuario;

    // Historial clínico
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL)
    @ToString.Exclude //
    @EqualsAndHashCode.Exclude // <--- RECOMENDADO: Evita bucles si Historial tiene referencia a Paciente
    private HistorialClinico historial;

    // Citas
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // <--- AÑADIR ESTO (Mejora rendimiento y evita errores)
    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas;
}