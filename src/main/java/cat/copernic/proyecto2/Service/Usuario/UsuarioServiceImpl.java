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
    public Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni) {
        Usuario nuevoUsuario = new Usuario();
        String nombreLimpio = nombre.toLowerCase().trim();
        String primerApellido = apellidos.toLowerCase().trim().split(" ")[0];
        String username = generarUsernameUnico(nombreLimpio, primerApellido);
        nuevoUsuario.setUsername(username);
        String passwordCifrado = passwordEncoder.encode(dni);
        nuevoUsuario.setPasswordHash(passwordCifrado);
        nuevoUsuario.setActivo(true);
        Rol rolPaciente = rolDAO.findByNombre("PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error: El rol 'PACIENTE' no existe"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolPaciente);
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