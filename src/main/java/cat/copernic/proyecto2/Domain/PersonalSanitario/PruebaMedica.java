package cat.copernic.proyecto2.Domain.PersonalSanitario;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name="PruebaMedica")
@Entity
public class PruebaMedica {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episodio_id")
    private EpisodioClinico episodio;

    private String tipo;
    private LocalDateTime fecha;
    private String resultados;
}

