package cat.copernic.proyecto2.Domain.Personal_sanitario;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="EpisodioClinico")
public class EpisodioClinico {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historial_id")
    private HistorialClinico historial;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado medico;

    private LocalDateTime fecha;

    private String motivoConsulta;
    private String diagnostico;
    private String observaciones;

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL)
    private List<PruebaMedica> pruebas;

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL)
    private List<Tratamiento> tratamientos;

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL)
    private List<RecetaMedica> recetas;
}

