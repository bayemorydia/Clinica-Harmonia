package cat.copernic.proyecto2.Domain.Administración;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import java.util.List;
import java.util.ArrayList;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name="Empleado")
public class Empleado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String dni;
    private String email;
    private String telefono;

    private LocalDate fechaAlta;
    private LocalDate fechaBaja;

    private String departamento;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    private BigDecimal salarioBase;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    private List<TurnoTrabajo> turnos = new ArrayList<>();

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    private List<Nomina> nominas = new ArrayList<>();

    // @OneToMany(mappedBy = "profesional")
    // private List<Cita> citasAtendidas = new ArrayList<>();



}
