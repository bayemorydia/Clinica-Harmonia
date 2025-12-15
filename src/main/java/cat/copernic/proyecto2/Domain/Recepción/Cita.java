package cat.copernic.proyecto2.Domain.Recepción;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Farmacia.LineaPedidoProveedor;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
@Entity
@Table(name="Cita")
public class Cita {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id")
    private Empleado profesional; // Médico o enfermero

    private LocalDateTime fechaHora;

    private String especialidad;
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    private String notasRecepcion;
    private String notasProfesional;
}
