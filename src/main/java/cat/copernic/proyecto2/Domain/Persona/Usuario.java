package cat.copernic.proyecto2.Domain.Persona;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="usuario_id")
    private Long id;
    private String username;
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    private boolean activo;


    public void cambiarPassword(String nuevoHash) {
    }

    public boolean tieneRol(Rol rol) {
        return true;
    }

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Empleado empleado;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Paciente paciente;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles;

}
