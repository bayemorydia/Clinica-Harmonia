package cat.copernic.proyecto2.Domain.Personal_sanitario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Farmacia.LineaPedidoProveedor;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
@Entity
@Table(name="Empleado")
public class RecetaMedica {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado medico;

    private LocalDate fechaEmision;
    private boolean activa = true;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL)
    private List<LineaReceta> lineas;
}
