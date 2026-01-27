package cat.copernic.proyecto2.Service.Usuario;

import cat.copernic.proyecto2.DAO.JPA.RolDAO;
import cat.copernic.proyecto2.DAO.JPA.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import org.springframework.context.annotation.Lazy;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolDAO rolDAO;

    @Autowired
    @Lazy
    private EmpleadoService empleadoService;

    @Override
    @Transactional
    public void activarUsuario(Long id) {
        Usuario u = usuarioDAO.findById(id).orElse(null);
        if (u != null) {
            u.setActivo(true);
            usuarioDAO.save(u);
        }
    }

    @Override
    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario u = usuarioDAO.findById(id).orElse(null);
        if (u != null) {
            u.setActivo(false);
            usuarioDAO.save(u);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarSolicitudReset(String dni) throws Exception {
        Empleado empleado = empleadoService.obtenerPorDni(dni);
        if (empleado == null)
            throw new Exception("DNI/NIE no encontrado");
        if (empleado.getUsuario() == null)
            throw new Exception("El empleado no tiene usuario asociado");
    }

    @Override
    @Transactional
    public void resetearPassword(String dni, String newPassword, String confirmPassword) throws Exception {
        if (!newPassword.equals(confirmPassword))
            throw new Exception("Las contraseñas no coinciden");
        Empleado empleado = empleadoService.obtenerPorDni(dni);
        if (empleado == null || empleado.getUsuario() == null)
            throw new Exception("Error al procesar la solicitud");

        Usuario usuario = empleado.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioDAO.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarPaginado(String query, Boolean activo, Pageable pageable) {
        return usuarioDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long id) {
        return usuarioDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Empleado obtenerEmpleadoPorUsername(String username) {
        var page = usuarioDAO.findByUsername(username, PageRequest.of(0, 1));
        if (page.hasContent()) {
            return page.getContent().get(0).getEmpleado();
        }
        return null;
    }

    @Override
    @Transactional
    public Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni) {
        return crearUsuarioBase(nombre, apellidos, dni, "PACIENTE");
    }

    @Override
    @Transactional
    public Usuario crearUsuarioEmpleado(String nombre, String apellidos, String dni, String rolNombre) {
        return crearUsuarioBase(nombre, apellidos, dni, rolNombre);
    }

    @Override
    @Transactional
    public void actualizarRolUsuario(Long usuarioId, String nuevoRolNombre) {
        Usuario usuario = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Rol nuevoRol = rolDAO.findByNombre(nuevoRolNombre)
                .orElseThrow(() -> new RuntimeException("El rol '" + nuevoRolNombre + "' no existe"));
        usuario.getRoles().clear();
        usuario.getRoles().add(nuevoRol);
        usuarioDAO.save(usuario);
    }

    private Usuario crearUsuarioBase(String nombre, String apellidos, String dni, String rolNombre) {
        Usuario nuevoUsuario = new Usuario();
        String nombreLimpio = nombre.toLowerCase().trim();
        String primerApellido = apellidos.toLowerCase().trim().split(" ")[0];
        String username = generarUsernameUnico(nombreLimpio, primerApellido);
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(dni.toUpperCase().trim()));
        nuevoUsuario.setActivo(true);
        Rol rol = rolDAO.findByNombre(rolNombre.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Error: El rol '" + rolNombre + "' no existe"));
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        nuevoUsuario.setRoles(roles);
        return usuarioDAO.save(nuevoUsuario);
    }

    private String generarUsernameUnico(String nombre, String apellido) {
        String base = nombre.substring(0, 1) + apellido;
        String intento = base;
        int contador = 1;
        while (usuarioDAO.existsByUsername(intento)) {
            intento = base + contador++;
        }
        return intento;
    }
}