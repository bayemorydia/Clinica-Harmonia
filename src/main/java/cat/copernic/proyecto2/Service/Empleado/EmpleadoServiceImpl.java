package cat.copernic.proyecto2.Service.Empleado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import cat.copernic.proyecto2.DAO.JPA.EmpleadoDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Rol.RolService;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Autowired
    private RolService rolService;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public Empleado crear(Empleado empleado) throws Exception {
        // Validaciones existentes
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

        // LÓGICA DE NEGOCIO MOVIDA DEL CONTROLLER
        Rol rol = rolService.encontrarRol(empleado.getRol().getId());
        empleado.setRol(rol);

        Usuario usuarioCreado = usuarioService.crearUsuarioEmpleado(
                empleado.getNombre(),
                empleado.getApellidos(),
                empleado.getDni(),
                rol.getNombre());

        empleado.setUsuario(usuarioCreado);
        // FIN LÓGICA MOVIDA

        empleado.setId(null);
        empleado.setFecha_alta(LocalDate.now());
        empleado.setActiva(true);
        empleado.setFecha_baja(null);
        return empleadoDAO.save(empleado);
    }

    @Override
    public Empleado actualizar(Long id, Empleado datos) throws Exception {
        Empleado existente = obtenerPorId(id);

        empleadoDAO.findByDni(datos.getDni()).ifPresent(e -> {
            if (!e.getId().equals(id))
                throw new IllegalArgumentException("❌ El DNI ya está en uso");
        });

        // LÓGICA DE NEGOCIO MOVIDA DEL CONTROLLER
        Rol rolNuevo = rolService.encontrarRol(datos.getRol().getId());

        if (existente.getUsuario() != null) {
            usuarioService.actualizarRolUsuario(existente.getUsuario().getId(),
                    rolNuevo.getNombre());
        }
        // FIN LÓGICA MOVIDA

        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setDni(datos.getDni());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setRol(rolNuevo); // Usamos el rol gestionado arriba
        existente.setSalarioBase(datos.getSalarioBase());
        existente.setInicio(datos.getInicio());
        existente.setFin(datos.getFin());
        existente.setTurno(datos.getTurno());

        return empleadoDAO.save(existente);
    }

    @Override
    public void activarEmpleado(Long id) {
        Empleado emp = empleadoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        emp.setActiva(true);
        emp.setFecha_baja(null);

        // Sincronización: Activar usuario vinculado
        if (emp.getUsuario() != null) {
            emp.getUsuario().setActivo(true);
        }

        empleadoDAO.save(emp);
    }

    @Override
    public void desactivarEmpleado(Long id) {
        Empleado existente = obtenerPorId(id);

        existente.setActiva(false);
        existente.setFecha_baja(LocalDate.now());

        // Sincronización: Desactivar usuario vinculado
        if (existente.getUsuario() != null) {
            existente.getUsuario().setActivo(false);
        }

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

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerPorDni(String dni) {
        return empleadoDAO.obtenerPorDni(dni);
    }

    // ... dentro de EmpleadoServiceImpl ...

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticasDashboard() {
        List<Empleado> todos = empleadoDAO.findAll();
        long total = todos.size();
        long activos = todos.stream().filter(Empleado::getActiva).count();
        long inactivos = total - activos;

        return Map.of(
                "total", total,
                "activos", activos,
                "inactivos", inactivos);
    }
}