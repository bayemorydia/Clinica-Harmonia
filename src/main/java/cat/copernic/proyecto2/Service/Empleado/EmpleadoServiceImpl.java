package cat.copernic.proyecto2.Service.Empleado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import cat.copernic.proyecto2.DAO.JPA.EmpleadoDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Override
    public Empleado crear(Empleado empleado) {
        if (empleadoDAO.findByDni(empleado.getDni()).isPresent())
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese DNI");
        if (empleadoDAO.findByEmail(empleado.getEmail()).isPresent())
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese Email");
        if (empleadoDAO.findByTelefono(empleado.getTelefono()).isPresent())
            throw new IllegalArgumentException("❌ Ya existe un Empleado con ese Teléfono");

        if (empleado.getTurno() == null)
            throw new IllegalArgumentException("❌ Turno no asignado");
        if (empleado.getInicio() == null || empleado.getFin() == null)
            throw new IllegalArgumentException("❌ Debes asignar hora de inicio y fin");

        empleado.setId(null);
        empleado.setFecha_alta(LocalDate.now());
        empleado.setActiva(true);
        return empleadoDAO.save(empleado);
    }

    @Override
    public Empleado actualizar(Long id, Empleado datos) {
        Empleado existente = obtenerPorId(id);

        empleadoDAO.findByDni(datos.getDni()).ifPresent(e -> {
            if (!e.getId().equals(id))
                throw new IllegalArgumentException("❌ El DNI ya está en uso");
        });

        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setDni(datos.getDni());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setRol(datos.getRol());
        existente.setSalarioBase(datos.getSalarioBase());
        existente.setFecha_baja(datos.getFecha_baja());
        existente.setInicio(datos.getInicio());
        existente.setFin(datos.getFin());
        existente.setTurno(datos.getTurno());
        return empleadoDAO.save(existente);
    }

    @Override
    @Transactional
    public void activarEmpleado(Long id) {
        Empleado emp = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setActiva(true);
        emp.setFecha_baja(null);
        // Al estar dentro de @Transactional, JPA sincroniza el cambio automáticamente
        // al finalizar el método
        empleadoDAO.save(emp);
    }

    @Override
    public void desactivarEmpleado(Long id) {
        Empleado existente = obtenerPorId(id);
        existente.setActiva(false);
        existente.setFecha_baja(LocalDate.now());
        empleadoDAO.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Long id) {
        return empleadoDAO.findById(id).orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empleado> getEmpleadosPaginados(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending());
        // Se mapea 'activo' de la vista al campo 'activa' del DAO
        return empleadoDAO.findByFilters(query, activo, pageable);
    }

    @Override
    public long ContarEmpleados() {
        return empleadoDAO.countByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorUsername(String username) {
        return empleadoDAO.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size) {
        return empleadoDAO.findByEmpleado(empleadoId, search, PageRequest.of(page, size));
    }
    @Transactional(readOnly = true)
    public Empleado obtenerPorDni(String dni) {
        return empleadoDAO.obtenerPorDni(dni);
    }

}