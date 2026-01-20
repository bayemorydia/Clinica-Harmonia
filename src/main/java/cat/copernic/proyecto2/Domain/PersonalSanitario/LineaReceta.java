package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
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

    @ManyToOne
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;
    
    private String dosis; 
}