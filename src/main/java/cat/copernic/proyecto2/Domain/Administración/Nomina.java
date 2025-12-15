package cat.copernic.proyecto2.Domain.Administración;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Entity
@Table(name="Nomina")

public class Nomina {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private YearMonth periodo;

    private BigDecimal salarioBase;
    private BigDecimal complementos;
    private BigDecimal deducciones;
    private BigDecimal totalPagar;


}

