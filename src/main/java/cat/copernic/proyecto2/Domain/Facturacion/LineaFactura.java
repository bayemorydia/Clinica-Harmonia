package cat.copernic.proyecto2.Domain.Facturacion;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;

@Data
@Entity
@Table(name="LineaFactura")
public class LineaFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Cambiado de int a Long para ser consistente con Factura e IDs de JPA

    private String concepto; // Añadido: muy útil para facturas manuales o servicios

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Medicamento producto; // Opcional, por si la línea es un medicamento

    private int cantidad;
    private float precioUnitario;
    private float subtotal;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    @ToString.Exclude // Evita errores de recursividad infinita con Lombok al imprimir la factura
    private Factura factura;
}