package cat.copernic.proyecto2.Domain.Recepción;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import cat.copernic.proyecto2.Domain.Personal_sanitario.HistorialClinico;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import java.util.List;
@Data
@Entity


public class Paciente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String dni;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String direccion;
    private String numeroSeguridadSocial;

    // Relación con login
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Historial clínico
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL)
    private HistorialClinico historial;

    // Citas
    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas;
}

