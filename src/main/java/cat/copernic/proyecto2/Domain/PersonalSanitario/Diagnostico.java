package cat.copernic.proyecto2.Domain.PersonalSanitario;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "diagnostico")
public class Diagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(unique = true, nullable = false)
    private String nombre;

    private boolean activo = true;

    @OneToMany(mappedBy = "diagnostico")
    private List<EpisodioClinico> episodios;
}
