package cat.copernic.proyecto2.Domain.Administración;

import cat.copernic.proyecto2.Domain.Recepción.Cita;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalTime;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
    @Pattern(regexp = "^([0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]|[XYZ][0-9]{7}[TRWAGMYFPDXBNJZSQVHLCKE])$", message = "Formato de DNI o NIE no válido")
    @Column(unique = true, nullable = false)
    private String dni;
    @Email(message = "Formato de email incorrecto")
    private String email;
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos numéricos")
    private String telefono;


    private LocalDate fecha_alta;
    private LocalDate fecha_baja;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @NotNull(message = "El salario base es obligatorio")
    private BigDecimal salarioBase;
    @Column(nullable = false)
    private Boolean activa = true;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;


    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime inicio ;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalTime fin;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    private Turno turno ;  // valor por defecto en Java


    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
    private List<Nomina> nominas = new ArrayList<>();

    @OneToMany(mappedBy = "profesional")
    private List<Cita> citasAtendidas = new ArrayList<>();





}