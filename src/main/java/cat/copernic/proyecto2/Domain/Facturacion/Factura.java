package cat.copernic.proyecto2.Domain.Facturacion;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name="Factura")
public class Factura {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    private String tipo; // VENTA, COMPRA

    // ESTE ES EL CAMPO QUE TE FALTA PARA QUE EL SERVICE NO DE ERROR
    private boolean activo = true; 

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente; 

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor; 

    // He añadido orphanRemoval = true para que al borrar líneas de la lista se borren de la DB
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaFactura> lineas;
}