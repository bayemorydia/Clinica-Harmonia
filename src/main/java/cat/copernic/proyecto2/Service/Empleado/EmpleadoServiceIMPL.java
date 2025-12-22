package cat.copernic.proyecto2.Service.Empleado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import cat.copernic.proyecto2.DAO.EmpleadoDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private  EmpleadoDAO empleadoDAO;

    public Empleado crear(Empleado empleado) {
        if (empleadoDAO.obtenerDNI(empleado.getDni()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese dni");
        }
        if (empleadoDAO.obtenerEmail(empleado.getEmail()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese email");
        }
        if (empleadoDAO.obtenerTelefono(empleado.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese Telefono");
        }
        empleado.setId(null);
        empleado.setFecha_alta(LocalDate.now());
        empleado.setFecha_baja(null);
        return empleadoDAO.save(empleado);
    }

    public Empleado actualizar(Long id, Empleado datos) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        Empleado OtroDNI = empleadoDAO.obtenerDNI(datos.getDni()).orElse(null);
        if (OtroDNI != null && !OtroDNI.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El Dni ya está en uso");
        }

        Empleado OtroEmail = empleadoDAO.obtenerEmail(datos.getEmail()).orElse(null);
        if (OtroEmail != null && !OtroEmail.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El email ya está en uso");
        }

        Empleado OtroTelefono = empleadoDAO.obtenerTelefono(datos.getTelefono()).orElse(null);
        if (OtroEmail != null && !OtroTelefono.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El Telefono ya está en uso");
        }

        // Campos editables
        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setDni(datos.getDni());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setDepartamento(datos.getDepartamento());
        existente.setRol(datos.getRol());
        existente.setSalarioBase(datos.getSalarioBase());
        existente.setFecha_baja(datos.getFecha_baja());
        return empleadoDAO.save(existente);
    }

    public void desactivarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        // Borrado lógico: dar de baja
        existente.setFecha_baja(LocalDate.now());
        empleadoDAO.save(existente);
    }
    public void eliminarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        empleadoDAO.deleteById(id);

    }


    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Long id) {
        return empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoDAO.findAll();
    }

    // ---------- EXTRAS (RF-01 / RF-02) ----------

    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        return empleadoDAO.obtenerActivos();
    }

    @Transactional(readOnly = true)
    public List<Empleado> buscarPorRol(Rol rol) {
        return empleadoDAO.obtenerPorRol(rol);
    }

    @Transactional(readOnly = true)
    public List<Empleado> buscarPorDepartamento(String departamento) {
        return empleadoDAO.obtenerPorDepartamento(departamento);
    }

    @Transactional(readOnly = true)
    public List<Empleado> buscarPorNombreOApellidos(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return empleadoDAO.obtenerActivosPorNombreOApellido(
                termino
        );
    }
}
