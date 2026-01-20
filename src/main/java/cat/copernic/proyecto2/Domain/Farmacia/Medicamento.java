package cat.copernic.proyecto2.Domain.Farmacia;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ProductoSanitario")
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

    private boolean activo = true;
}