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

    @Override
    public Empleado crear(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("❌ El empleado no puede ser nulo");
        }

        if (empleadoDAO.obtenerDNI(empleado.getDni()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un empleado con ese DNI");
        }

        if (empleadoDAO.obtenerEmail(empleado.getEmail()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un empleado con ese email");
        }

        if (empleadoDAO.obtenerTelefono(empleado.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("❌ Ya existe un empleado con ese teléfono");
        }

        if (empleado.getTurno() == null) {
            throw new IllegalArgumentException("❌ El empleado debe tener un turno asignado");
        }

        if (empleado.getInicio() == null || empleado.getFin() == null) {
            throw new IllegalArgumentException("❌ Debes asignar hora de inicio y fin");
        }

        if (empleado.getInicio().isAfter(empleado.getFin())) {
            throw new IllegalArgumentException("❌ La hora de inicio no puede ser posterior a la hora de fin");
        }

        try {
            empleado.setId(null);
            empleado.setFecha_alta(LocalDate.now());
            empleado.setFecha_baja(null);
            return empleadoDAO.save(empleado);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al guardar el empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public Empleado actualizar(Long id, Empleado datos) {
        if (datos == null) {
            throw new IllegalArgumentException("❌ Los datos del empleado no pueden ser nulos");
        }

        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Empleado no encontrado con id " + id));

        // Validaciones únicas
        Empleado otroDNI = empleadoDAO.obtenerDNI(datos.getDni()).orElse(null);
        if (otroDNI != null && !otroDNI.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El DNI ya está en uso");
        }

        Empleado otroEmail = empleadoDAO.obtenerEmail(datos.getEmail()).orElse(null);
        if (otroEmail != null && !otroEmail.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El email ya está en uso");
        }

        Empleado otroTelefono = empleadoDAO.obtenerTelefono(datos.getTelefono()).orElse(null);
        if (otroTelefono != null && !otroTelefono.getId().equals(id)) {
            throw new IllegalArgumentException("❌ El teléfono ya está en uso");
        }

        if (datos.getInicio() == null || datos.getFin() == null) {
            throw new IllegalArgumentException("❌ Debes asignar hora de inicio y fin");
        }

        if (datos.getInicio().isAfter(datos.getFin())) {
            throw new IllegalArgumentException("❌ La hora de inicio no puede ser posterior a la hora de fin");
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

        try {
            return empleadoDAO.save(existente);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al actualizar el empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public void desactivarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Empleado no encontrado con id " + id));

        existente.setActiva(false);
        existente.setFecha_baja(LocalDate.now());

        try {
            empleadoDAO.save(existente);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al desactivar el empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarEmpleado(Long id) {
        Empleado existente = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Empleado no encontrado con id " + id));

        try {
            empleadoDAO.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al eliminar el empleado: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Long id) {
        return empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Empleado no encontrado con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        try {
            return empleadoDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al listar empleados: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        try {
            return empleadoDAO.obtenerActivos();
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al listar empleados activos: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorRol(Rol rol) {
        try {
            return empleadoDAO.obtenerPorRol(rol);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al buscar empleados por rol: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorDepartamento(String departamento) {
        try {
            return empleadoDAO.obtenerPorDepartamento(departamento);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al buscar empleados por departamento: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorNombreOApellidos(String termino) {
        try {
            if (termino == null || termino.isBlank()) {
                return listarTodos();
            }
            return empleadoDAO.obtenerActivosPorNombreOApellido(termino);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al buscar empleados por nombre/apellido: " + e.getMessage(), e);
        }
    }

    @Override
    public long ContarEmpleados() {
        try {
            return empleadoDAO.BuscarEmpleadosTotal();
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al contar empleados: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorUsername(String username) {
        return empleadoDAO.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("❌ No se encontró un empleado para el usuario: " + username));
    }

    @Override
    public Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            if (search != null && search.isEmpty()) {
                search = null;
            }
            return empleadoDAO.findByEmpleado(empleadoId, search, pageable);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al listar nóminas del empleado: " + e.getMessage(), e);
        }
    }
}
