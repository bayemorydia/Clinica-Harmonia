package cat.copernic.proyecto2.Domain.Administración;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Por ejemplo: "DIURNO" o "NOCTURNO"
}
