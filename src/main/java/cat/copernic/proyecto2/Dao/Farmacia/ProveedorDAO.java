package cat.copernic.proyecto2.Dao.Farmacia;


import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProveedorDAO extends JpaRepository<Proveedor, Long> {
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true")
    List<Proveedor> BuscarProveedorActivo();

    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER('%valor%') OR LOWER(p.cif) LIKE LOWER('%valor%')")
    List<Proveedor> buscarProveedorPorNombreOCif(@Param("valor") String valor);
    //Buscar Proovedor por el cif tanto por mayusculas i minusculas
    //param para indicar que ese paremetro se usara en la base datos
    @Query("SELECT p FROM Proveedor p WHERE p.email = :valor")
    Optional<Proveedor> BuscarProveedorEmail(@Param("valor") String valor);

    @Query("SELECT p FROM Proveedor p WHERE p.cif = :valor")
    Optional<Proveedor> BuscarProveedorCif(@Param("valor") String valor);

}
