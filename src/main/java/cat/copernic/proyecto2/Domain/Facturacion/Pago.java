package cat.copernic.proyecto2.Domain.Facturacion;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.math.BigDecimal;

@Data
@Entity
@Table(name="Pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Factura que se está pagando
    @ManyToOne
    @JoinColumn(name = "factura_id")
    private Factura factura;

    // (Opcional) paciente que realiza el pago (normalmente el mismo de la factura)
    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    private BigDecimal importe;
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;
}
