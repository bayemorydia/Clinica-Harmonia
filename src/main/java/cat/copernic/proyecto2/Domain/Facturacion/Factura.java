package cat.copernic.proyecto2.Domain.Facturacion;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import java.util.List;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Entity
@Table(name="Factura")
public class Factura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    private String tipo; // VENTA, COMPRA

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente; // opcional

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor; // opcional

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL)
    private List<LineaFactura> lineas;
}
