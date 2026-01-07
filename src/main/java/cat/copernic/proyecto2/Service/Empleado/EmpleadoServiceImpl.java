package cat.copernic.proyecto2.Service.Empleado;

import cat.copernic.proyecto2.Dao.EmpleadoDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Override
    public Empleado crear(Empleado empleado) {

        if (empleadoDAO.obtenerDNI(empleado.getDni()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese DNI");
        }

        if (empleado.getEmail() != null && empleadoDAO.obtenerEmail(empleado.getEmail()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese email");
        }

        if (empleadoDAO.obtenerTelefono(empleado.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese teléfono");
        }

        // ✅ Campos controlados por el sistema (NO por el form)
        empleado.setId(null);
        empleado.setFecha_alta(LocalDate.now());
        empleado.setFecha_baja(null);
        empleado.setActiva(true);

        return empleadoDAO.save(empleado);
    }

    @Override
    public Empleado actualizar(Long id, Empleado datos) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        // ---- Unicidad DNI ----
        Empleado otroDni = empleadoDAO.obtenerDNI(datos.getDni()).orElse(null);
        if (otroDni != null && !otroDni.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El DNI ya está en uso");
        }

        // ---- Unicidad Email (si viene informado) ----
        if (datos.getEmail() != null) {
            Empleado otroEmail = empleadoDAO.obtenerEmail(datos.getEmail()).orElse(null);
            if (otroEmail != null && !otroEmail.getId().equals(id)) {
                throw new IllegalArgumentException("❌ El email ya está en uso");
            }
        }

        // ---- Unicidad Teléfono ----
        Empleado otroTelefono = empleadoDAO.obtenerTelefono(datos.getTelefono()).orElse(null);
        if (otroTelefono != null && !otroTelefono.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El teléfono ya está en uso");
        }

        // ✅ Campos editables
        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setDni(datos.getDni());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setDepartamento(datos.getDepartamento());
        existente.setRol(datos.getRol());
        existente.setSalarioBase(datos.getSalarioBase());

        // ❌ MUY IMPORTANTE: NO permitir modificar fecha_baja desde actualizar
        // existente.setFecha_baja(datos.getFecha_baja());  // <- eliminado
        // existente.setActiva(...);                        // <- eliminado

        return empleadoDAO.save(existente);
    }

    @Override
    public void desactivarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        existente.setFecha_baja(LocalDate.now());
        existente.setActiva(false);

        empleadoDAO.save(existente);
    }

    @Override
    public void activarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        existente.setFecha_baja(null);
        existente.setActiva(true);

        empleadoDAO.save(existente);
    }

    @Override
    public void eliminarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        empleadoDAO.delete(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Long id) {
        return empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        return empleadoDAO.obtenerActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorRol(Rol rol) {
        return empleadoDAO.obtenerPorRol(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorDepartamento(String departamento) {
        return empleadoDAO.obtenerPorDepartamento(departamento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorNombreOApellidos(String termino) {
        if (termino == null || termino.isBlank()) return listarTodos();
        return empleadoDAO.obtenerActivosPorNombreOApellido(termino);
    }




}
