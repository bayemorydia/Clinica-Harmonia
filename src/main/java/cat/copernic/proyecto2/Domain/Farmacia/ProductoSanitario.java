package cat.copernic.proyecto2.Domain.Farmacia;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import cat.copernic.proyecto2.Domain.Farmacia.LineaPedidoProveedor;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
@Entity
@Table(name="ProductoSanitario")

public class ProductoSanitario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;
    private String tipo;
    private int stockActual;
    private int stockMinimo;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<LineaPedidoProveedor> lineasPedido;

    // 2️⃣ Relación con LineaFactura (FACTURACIÓN DE PRODUCTOS)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<LineaFactura> lineasFactura;
}

