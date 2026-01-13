package cat.copernic.proyecto2.Service.Proveedor;

import java.util.List;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;

public interface ProveedorService {
    Proveedor crearProveedor(Proveedor proveedor);

    Proveedor actualizarProveedor(Long id, Proveedor datosProveedor);

    void DesactivarProveedor(Long id);

    void activarProveedor(Long id);

    void EliminarProveedor(Long id);

    Proveedor buscarPorID(Long id);

    List<Proveedor> listarTodos();

    List<Proveedor> listarActivos();

    List<Proveedor> buscarPorNombreOCif(String termino);

}
