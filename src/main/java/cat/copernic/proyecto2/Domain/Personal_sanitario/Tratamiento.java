package cat.copernic.proyecto2.Domain.Personal_sanitario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Farmacia.LineaPedidoProveedor;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
@Entity
@Table(name="Tratamiento")
public class Tratamiento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado prescriptor;

    private String indicaciones;
}
