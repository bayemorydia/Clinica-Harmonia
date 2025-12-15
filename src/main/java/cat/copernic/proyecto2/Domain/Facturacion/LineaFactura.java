package cat.copernic.proyecto2.Domain.Facturacion;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import cat.copernic.proyecto2.Domain.Farmacia.ProductoSanitario;
@Data
@Entity
@Table(name="LineaFactura")
public class LineaFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idDetalle;
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoSanitario producto;
    private int cantidad;
    private float precioUnitario;
    private float subtotal;

    // Factura "1" -- "*" DetalleFactura
    @ManyToOne
    @JoinColumn(name = "factura_id")
    private Factura factura;



}
