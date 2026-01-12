package cat.copernic.proyecto2.Domain.Farmacia;

import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@Entity
@Table(name="ProductoSanitario")

public class Medicamento {

@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El código no puede estar vacío")
    @Size(min = 3, max = 15, message = "El código debe tener entre 3 y 15 caracteres")
    private String codigo;

    @NotBlank(message = "Debes especificar un tipo o categoría")
    private String tipo;

    @Min(value = 0, message = "El stock actual no puede ser inferior a 0")
    private int stockActual;

    @Min(value = 0, message = "El stock mínimo no puede ser inferior a 0")
    private int stockMinimo;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<LineaPedidoProveedor> lineasPedido;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<LineaFactura> lineasFactura;
}

