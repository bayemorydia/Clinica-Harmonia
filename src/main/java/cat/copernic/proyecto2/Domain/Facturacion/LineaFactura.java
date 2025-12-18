package cat.copernic.proyecto2.Domain.Facturacion;

import jakarta.persistence.*;
import lombok.Data;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;

@Data
@Entity
@Table(name="LineaFactura")
public class LineaFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idDetalle;
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Medicamento producto;
    private int cantidad;
    private float precioUnitario;
    private float subtotal;

    // Factura "1" -- "*" DetalleFactura
    @ManyToOne
    @JoinColumn(name = "factura_id")
    private Factura factura;



}
