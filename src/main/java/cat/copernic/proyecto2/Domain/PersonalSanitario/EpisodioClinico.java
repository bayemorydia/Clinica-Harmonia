package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "EpisodioClinico")
public class EpisodioClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historial_id")
    private HistorialClinico historial;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Empleado medico;

    private LocalDateTime fecha;
    private String motivoConsulta;

    @ManyToMany
    @JoinTable(
        name = "episodio_pruebas", 
        joinColumns = @JoinColumn(name = "episodio_id"), 
        inverseJoinColumns = @JoinColumn(name = "prueba_id")
    )
    private List<PruebaMedica> pruebasAsignadas = new ArrayList<>();

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL)
    private List<Tratamiento> tratamientos = new ArrayList<>();

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL)
    private List<RecetaMedica> recetas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "diagnostico_id")
    private Diagnostico diagnostico;
}