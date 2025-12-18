package cat.copernic.proyecto2.Domain.Farmacia;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

import java.util.List;
@Data
@Entity
@Table(name="Proveedor")

public class Proveedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String cif;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDate fecha_alta;
    private LocalDate fecha_baja;
    private boolean activo = true;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<PedidoProveedor> pedidos;


}