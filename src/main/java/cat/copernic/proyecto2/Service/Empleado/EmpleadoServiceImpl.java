package cat.copernic.proyecto2.Service.Empleado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import cat.copernic.proyecto2.DAO.EmpleadoDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

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

        if (empleado.getTurno() == null) {
            System.err.println("ERROR: Turno no asignado");
            throw new IllegalArgumentException("❌ El empleado debe tener un turno asignado");
        }

        if (empleado.getInicio() == null || empleado.getFin() == null) {
            System.err.println("ERROR: Horario incompleto → Inicio: " + empleado.getInicio() + ", Fin: " + empleado.getFin());
            throw new IllegalArgumentException("❌ Debes asignar hora de inicio y fin");
        }
        empleado.setId(null);
        empleado.setFecha_alta(LocalDate.now());
        empleado.setFecha_baja(null);
        System.out.println("DEBUG: Guardando empleado → " + empleado);

        try {
            System.out.println("echo");
            return empleadoDAO.save(empleado);
        } catch (Exception e) {
            System.err.println("ERROR: Fallo al guardar el empleado");
            e.printStackTrace();
            throw e;  // re-lanzamos para que el error llegue al log
        }
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
        if (OtroTelefono != null && !OtroTelefono.getId().equals(id)) {
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
        existente.setInicio(datos.getInicio());
        existente.setFin(datos.getFin());
        existente.setTurno(datos.getTurno());
        return empleadoDAO.save(existente);
    }

    public void desactivarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id " + id));

        // Borrado lógico: dar de baja
        existente.setActiva(false);
        existente.setFecha_baja(LocalDate.now());
        empleadoDAO.save(existente);
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
                termino);
    }

    public long ContarEmpleados() {
        return empleadoDAO.BuscarEmpleadosTotal();
    }

    // En EmpleadoServiceImpl.java
    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorUsername(String username) {
        return empleadoDAO.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("No se encontró un empleado para el usuario: " + username));
    }


    @Override
    public Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search == null || search.isEmpty()) {
            search = null;
        }
        return empleadoDAO.findByEmpleado(empleadoId, search, pageable);
    }

}