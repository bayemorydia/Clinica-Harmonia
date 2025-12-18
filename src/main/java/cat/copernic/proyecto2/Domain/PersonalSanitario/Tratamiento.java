package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name="Tratamiento")
public class Tratamiento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado prescriptor;

    private String indicaciones;
}
