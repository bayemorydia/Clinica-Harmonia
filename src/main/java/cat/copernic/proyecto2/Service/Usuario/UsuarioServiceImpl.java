package cat.copernic.proyecto2.Service.Usuario;

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
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolDAO rolDAO;

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarUsuarios(Pageable pageable) {
        return usuarioDAO.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorUsername(String query, Pageable pageable) {
        return usuarioDAO.findByUsernameContainingIgnoreCase(query, pageable);
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
    @Transactional
    public void guardar(Usuario usuario) {
        usuarioDAO.save(usuario);
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

    private Usuario crearUsuarioBase(String nombre, String apellidos, String dni, String rolNombre) {
        Usuario nuevoUsuario = new Usuario();
        String nombreLimpio = nombre.toLowerCase().trim();
        String primerApellido = apellidos.toLowerCase().trim().split(" ")[0];
        String username = generarUsernameUnico(nombreLimpio, primerApellido);
        
        nuevoUsuario.setUsername(username);
        // Ciframos el DNI como contraseña inicial
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(dni.toUpperCase().trim()));
        nuevoUsuario.setActivo(true);

        Rol rol = rolDAO.findByNombre(rolNombre.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Error: El rol '" + rolNombre + "' no existe"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        nuevoUsuario.setRoles(roles);

        return usuarioDAO.save(nuevoUsuario);
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