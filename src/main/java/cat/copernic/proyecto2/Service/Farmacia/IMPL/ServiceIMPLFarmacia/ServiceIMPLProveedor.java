package cat.copernic.proyecto2.Service.Farmacia.IMPL.ServiceIMPLFarmacia;
import cat.copernic.proyecto2.Dao.Farmacia.ProveedorDAO;
import cat.copernic.proyecto2.Service.Farmacia.ServiceProveedor;
import org.springframework.transaction.annotation.Transactional;
import cat.copernic.proyecto2.Dao.Farmacia.ProveedorDAO;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import cat.copernic.proyecto2.Domain.Farmacia.Proveedor;
import java.lang.IllegalArgumentException;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class ServiceIMPLProveedor implements ServiceProveedor {
    //read only solo lee datos

    @Autowired
    private  ProveedorDAO ProveedorDao;

    public Proveedor crearProveedor(Proveedor proveedor) {
            if (ProveedorDao.BuscarProveedorCif(proveedor.getCif()).isPresent()) {
                throw new IllegalArgumentException("❌ Ya existe un proveedor con ese CIF");
            }
        if (ProveedorDao.BuscarProveedorEmail(proveedor.getEmail()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un proveedor con ese email");
        }
        if (ProveedorDao.BuscarProveedorNumero(proveedor.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un proveedor con ese Telefono");
        }
        proveedor.setId(null);
        proveedor.setActivo(true);
        proveedor.setFecha_alta(LocalDate.now());
        return ProveedorDao.save(proveedor);
    }

    public Proveedor actualizarProveedor(Long id, Proveedor datosProveedor) {
        Proveedor existente = ProveedorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        Proveedor OtroCif = ProveedorDao.BuscarProveedorCif(datosProveedor.getCif()).orElse(null);
        if (OtroCif != null && !OtroCif.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El CIF ya está en uso");
        }

        Proveedor OtroEmail = ProveedorDao.BuscarProveedorEmail(datosProveedor.getEmail()).orElse(null);
        if (OtroEmail != null && !OtroEmail.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El email ya está en uso");
        }

        Proveedor OtroTelefono = ProveedorDao.BuscarProveedorNumero(datosProveedor.getTelefono()).orElse(null);
        if (OtroEmail != null && !OtroEmail.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El email ya está en uso");
        }


        existente.setNombre(datosProveedor.getNombre());
        existente.setCif(datosProveedor.getCif());
        existente.setTelefono(datosProveedor.getTelefono());
        existente.setEmail(datosProveedor.getEmail());
        existente.setDireccion(datosProveedor.getDireccion());
        existente.setFecha_baja (datosProveedor.getFecha_baja());
        existente.setActivo(datosProveedor.isActivo());


        return ProveedorDao.save(existente);
    }

    public void DesactivarProveedor(Long id) {
        Proveedor existente=  ProveedorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

        existente.setActivo(false);
        existente.setFecha_baja(LocalDate.now());
        ProveedorDao.save(existente);
    }
    public void EliminarProveedor(Long id) {
        Proveedor existente = ProveedorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));
        ProveedorDao.deleteById(id);
    }

    @Transactional(readOnly = true)//solo va a leer datos
    public Proveedor obtenerPorId(Long id) {
        return ProveedorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
        return ProveedorDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return ProveedorDao.BuscarProveedorActivo();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> buscarPorNombreOCif(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
         if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return ProveedorDao.buscarProveedorPorNombreOCif(termino);
    }
    @Transactional(readOnly = true)
    public Proveedor buscarPorID(long id) {
        return  ProveedorDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id " + id));

    }
}
