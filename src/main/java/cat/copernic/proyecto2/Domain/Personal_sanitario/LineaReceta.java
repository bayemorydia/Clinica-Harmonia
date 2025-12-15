package cat.copernic.proyecto2.Domain.Personal_sanitario;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
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