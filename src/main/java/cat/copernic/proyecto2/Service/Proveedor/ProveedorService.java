package cat.copernic.proyecto2.Service.Proveedor;

import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProveedorService {
    Proveedor crearProveedor(Proveedor proveedor);
    Proveedor actualizarProveedor(Long id, Proveedor datosProveedor);
    void desactivarProveedor(Long id);
    void activarProveedor(Long id);
    Proveedor buscarPorID(Long id);
    List<Proveedor> listarTodos();
    Page<Proveedor> listarPaginado(String query, Boolean activo, Pageable pageable);
}