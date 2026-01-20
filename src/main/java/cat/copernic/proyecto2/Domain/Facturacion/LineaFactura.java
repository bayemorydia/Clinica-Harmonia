package cat.copernic.proyecto2.Domain.Facturacion;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name="LineaFactura")
public class LineaFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String concepto;
    private int cantidad;
    private float precioUnitario;
    private float subtotal;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    @ToString.Exclude
    private Factura factura;
}