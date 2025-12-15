package cat.copernic.proyecto2.Domain.Farmacia;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
@Entity
@Table(name="LineaPedidoProveedor")

public class LineaPedidoProveedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private PedidoProveedor pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoSanitario producto;

    private int cantidad;
    private BigDecimal precioUnitario;


}

