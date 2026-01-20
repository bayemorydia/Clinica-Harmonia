package cat.copernic.proyecto2.Domain.PersonalSanitario;

import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.util.List;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "HistorialClinico")
public class HistorialClinico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @OneToMany(mappedBy = "historial", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    private List<EpisodioClinico> episodios;
}