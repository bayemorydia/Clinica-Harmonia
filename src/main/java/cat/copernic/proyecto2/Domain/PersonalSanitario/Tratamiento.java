package cat.copernic.proyecto2.Domain.PersonalSanitario;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.io.Serializable;
// Importamos Empleado, que es la clase que tienes en tus logs
// Si Empleado está en otro paquete (ej. RecursosHumanos), ajusta este import:
import cat.copernic.proyecto2.Domain.Administración.Empleado; 

@Data
@Entity
@Table(name="Tratamiento")
public class Tratamiento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del tratamiento es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @Positive(message = "El precio debe ser un valor positivo")
    @NotNull(message = "El precio es obligatorio")
    private Double precio;

    private boolean activo = true; 

    // Relación con Episodio (para solucionar el error 'mappedBy episodio')
    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    // CORREGIDO: Usamos Empleado en lugar de PersonalSanitario
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; 
}