package cat.copernic.proyecto2.Service.Proveedor;

import cat.copernic.proyecto2.DAO.ProveedorDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    private ProveedorDAO proveedorDAO;

    public Proveedor crearProveedor(Proveedor proveedor) {

        if (proveedorDAO.BuscarProveedorCif(proveedor.getCif()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un proveedor con ese CIF");
        }

        if (proveedor.getEmail() != null && proveedorDAO.BuscarProveedorEmail(proveedor.getEmail()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un proveedor con ese email");
        }

        if (proveedorDAO.BuscarProveedorNumero(proveedor.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un proveedor con ese teléfono");
        }

        proveedor.setId(null);
        proveedor.setFecha_alta(LocalDate.now());
        proveedor.setFecha_baja(null);
        proveedor.setActivo(true);

        return proveedorDAO.save(proveedor);
    }

    public Proveedor actualizarProveedor(Long id, Proveedor datosProveedor) {
        Proveedor existente = proveedorDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        // ---- Unicidad CIF ----
        Proveedor otroCif = proveedorDAO.BuscarProveedorCif(datosProveedor.getCif()).orElse(null);
        if (otroCif != null && !otroCif.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El CIF ya está en uso");
        }

        // ---- Unicidad Email (si viene informado) ----
        if (datosProveedor.getEmail() != null) {
            Proveedor otroEmail = proveedorDAO.BuscarProveedorEmail(datosProveedor.getEmail()).orElse(null);
            if (otroEmail != null && !otroEmail.getId().equals(id)) {
                throw new IllegalArgumentException("❌ El email ya está en uso");
            }
        }

        // ---- Unicidad Teléfono ----
        Proveedor otroTelefono = proveedorDAO.BuscarProveedorNumero(datosProveedor.getTelefono()).orElse(null);
        if (otroTelefono != null && !otroTelefono.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El teléfono ya está en uso");
        }

        // ✅ Campos editables
        existente.setNombre(datosProveedor.getNombre());
        existente.setCif(datosProveedor.getCif());
        existente.setTelefono(datosProveedor.getTelefono());
        existente.setEmail(datosProveedor.getEmail());
        existente.setDireccion(datosProveedor.getDireccion());

        // ❌ No permitir modificar fecha_baja ni activo desde actualizar
        // existente.setFecha_baja(datosProveedor.getFecha_baja());
        // existente.setActivo(datosProveedor.isActivo());

        return proveedorDAO.save(existente);
    }

    public void DesactivarProveedor(Long id) {
        Proveedor existente = proveedorDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        existente.setFecha_baja(LocalDate.now());
        existente.setActivo(false);

        proveedorDAO.save(existente);
    }

    public void activarProveedor(Long id) {
        Proveedor existente = proveedorDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        existente.setFecha_baja(null);
        existente.setActivo(true);

        proveedorDAO.save(existente);
    }

    public void EliminarProveedor(Long id) {
        Proveedor existente = proveedorDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        proveedorDAO.delete(existente);
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorID(Long id) {
        return proveedorDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
        return proveedorDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorDAO.BuscarProveedorActivo();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> buscarPorNombreOCif(String termino) {
        if (termino == null || termino.isBlank()) return listarTodos();
        return proveedorDAO.buscarProveedorPorNombreOCif(termino);
    }

}
