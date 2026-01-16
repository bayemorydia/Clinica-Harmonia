package cat.copernic.proyecto2.Domain.PersonalSanitario;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="LineaReceta")
public class LineaReceta {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receta_id")
    private RecetaMedica receta;

    private String medicamento; // Nombre del medicamento del catálogo
    private String dosis;
    
    // Almacena el precio capturado del medicamento para la factura final
    private Double precioUnitario; 
}