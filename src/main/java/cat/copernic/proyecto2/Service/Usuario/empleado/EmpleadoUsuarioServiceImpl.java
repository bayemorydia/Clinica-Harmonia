package cat.copernic.proyecto2.Service.Usuario.empleado;

import cat.copernic.proyecto2.DAO.JPA.EmpleadoDAO;
import cat.copernic.proyecto2.DAO.JPA.RolDAO;
import cat.copernic.proyecto2.DAO.JPA.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class EmpleadoUsuarioServiceImpl implements EmpleadoUsuarioService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private RolDAO rolDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarUsuarios(Pageable pageable) {
        return usuarioDAO.findAll(pageable); // Devuelve todos, activos e inactivos
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorUsername(String query, Pageable pageable) {
        return usuarioDAO.findByUsernameContainingIgnoreCase(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long id) {
        return usuarioDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void guardar(Usuario usuario) {
        usuarioDAO.save(usuario);
    }

    @Override
    @Transactional
    public Usuario crearEmpleadoConUsuario(String nombre, String apellidos, String dni, String rolNombre) {
        // Crear Usuario asociado
        Usuario nuevoUsuario = new Usuario();
        String nombreLimpio = nombre.toLowerCase().trim();
        String primerApellido = apellidos.toLowerCase().trim().split(" ")[0];
        String username = generarUsernameUnico(nombreLimpio, primerApellido);
        nuevoUsuario.setUsername(username);

        // Usamos el DNI como contraseña inicial
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(dni));
        nuevoUsuario.setActivo(true);

        // Rol del usuario
        Rol rol = rolDAO.findByNombre(rolNombre)
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

    @Override
    @Transactional
    public void actualizarRolUsuario(Long usuarioId, String nuevoRolNombre) {

        Usuario usuario = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol nuevoRol = rolDAO.findByNombre(nuevoRolNombre)
                .orElseThrow(() -> new RuntimeException("El rol '" + nuevoRolNombre + "' no existe"));

        Set<Rol> nuevosRoles = new HashSet<>();
        nuevosRoles.add(nuevoRol);

        usuario.setRoles(nuevosRoles);

        usuarioDAO.save(usuario);
    }

}
