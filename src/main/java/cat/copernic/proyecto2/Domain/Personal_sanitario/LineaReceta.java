package cat.copernic.proyecto2.Domain.Personal_sanitario;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="LineaReceta")
public class LineaReceta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receta_id")
    private RecetaMedica receta;

    private String medicamento;
    private String dosis;
    private String frecuencia;
    private int duracionDias;
}