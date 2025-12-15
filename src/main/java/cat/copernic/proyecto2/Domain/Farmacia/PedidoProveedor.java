package cat.copernic.proyecto2.Domain.Farmacia;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
@Data
@Entity
@Table(name="PedidoProveedor")
public class PedidoProveedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    private LocalDate fechaPedido;
    private LocalDate fechaEntregaEstimada;

    private String estado; // PENDIENTE, RECIBIDO, CANCELADO...

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<LineaPedidoProveedor> lineas = new ArrayList<>();
}

