package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name="RecetaMedica") // Corregido: antes decía Empleado
public class RecetaMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado medico;

    private LocalDate fechaEmision;
    private boolean activa = true;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaReceta> lineas;
}