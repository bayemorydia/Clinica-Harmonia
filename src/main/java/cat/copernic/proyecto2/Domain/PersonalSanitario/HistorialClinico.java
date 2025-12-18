package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="HistorialClinico")
public class HistorialClinico {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @OneToMany(mappedBy = "historial", cascade = CascadeType.ALL)
    private List<EpisodioClinico> episodios;
}
