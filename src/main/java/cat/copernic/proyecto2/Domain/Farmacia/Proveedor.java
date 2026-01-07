package cat.copernic.proyecto2.Domain.Farmacia;

import cat.copernic.proyecto2.Domain.Farmacia.PedidoProveedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El CIF es obligatorio")
    @Column(unique = true, nullable = false)
    private String cif;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos numéricos")
    private String telefono;

    @Email(message = "Formato de email incorrecto")
    private String email;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private LocalDate fecha_alta;
    private LocalDate fecha_baja;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProveedor> pedidos = new ArrayList<>();
}
