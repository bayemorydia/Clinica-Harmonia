package cat.copernic.proyecto2.Domain.Farmacia;

import cat.copernic.proyecto2.Domain.Facturacion.LineaFactura;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name="ProductoSanitario")
public class Medicamento {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // Campo para borrado lógico (activar/desactivar)
    private boolean activo = true; 

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<LineaFactura> lineasFactura;
}
