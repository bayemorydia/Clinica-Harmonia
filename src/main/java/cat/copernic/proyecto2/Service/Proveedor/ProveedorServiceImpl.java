package cat.copernic.proyecto2.Service.Proveedor;

import cat.copernic.proyecto2.DAO.JPA.ProveedorDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    private ProveedorDAO proveedorDAO;

    @Override
    @Transactional(readOnly = true)
    public Page<Proveedor> listarPaginado(String query, Boolean activo, Pageable pageable) {
        return proveedorDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional
    public Proveedor crearProveedor(Proveedor proveedor) {
        if (proveedorDAO.findByCif(proveedor.getCif()).isPresent()) 
            throw new IllegalArgumentException("El CIF ya existe.");
        proveedor.setFecha_alta(LocalDate.now());
        proveedor.setActivo(true);
        return proveedorDAO.save(proveedor);
    }

    @Override
    @Transactional
    public Proveedor actualizarProveedor(Long id, Proveedor datos) {
        Proveedor ex = buscarPorID(id);
        ex.setNombre(datos.getNombre());
        ex.setCif(datos.getCif());
        ex.setTelefono(datos.getTelefono());
        ex.setEmail(datos.getEmail());
        ex.setDireccion(datos.getDireccion());
        return proveedorDAO.save(ex);
    }

    @Override
    @Transactional
    public void desactivarProveedor(Long id) {
        Proveedor p = buscarPorID(id);
        p.setActivo(false);
        p.setFecha_baja(LocalDate.now());
        proveedorDAO.save(p);
    }

    @Override
    @Transactional
    public void activarProveedor(Long id) {
        Proveedor p = buscarPorID(id);
        p.setActivo(true);
        p.setFecha_baja(null);
        proveedorDAO.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor buscarPorID(Long id) {
        return proveedorDAO.findById(id).orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() { return proveedorDAO.findAll(); }
}