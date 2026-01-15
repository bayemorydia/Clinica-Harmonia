package cat.copernic.proyecto2.Domain.Persona;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{error.rol.nombre.vacio}") // Validación para que no sea vacío
    @Size(min = 3, max = 50, message = "{error.rol.nombre.size}")
    @Column(unique = true, nullable = false)
    private String nombre;

    @NotBlank(message = "{error.rol.descripcion.vacio}")
    private String descripcion;

    private boolean activo = true;
}