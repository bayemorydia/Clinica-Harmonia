package cat.copernic.proyecto2.Domain.Farmacia;

import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="ProductoSanitario")

public class Medicamento {

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

