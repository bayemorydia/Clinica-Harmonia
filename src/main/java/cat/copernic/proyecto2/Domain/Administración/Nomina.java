package cat.copernic.proyecto2.Domain.Administración;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Nomina")
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El empleado es obligatorio")
    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @NotNull(message = "El período es obligatorio")
    private String periodo;
    
    @NotNull(message = "El salario base es obligatorio")
    private BigDecimal salarioBase;

    private BigDecimal complementos;

    private BigDecimal deducciones;

    @NotNull(message = "El total a pagar es obligatorio")
    private BigDecimal totalPagar;
}
