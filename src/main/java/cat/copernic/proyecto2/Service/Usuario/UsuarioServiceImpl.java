package cat.copernic.proyecto2.Service.Usuario;

import cat.copernic.proyecto2.DAO.RolDAO;
import cat.copernic.proyecto2.DAO.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private RolDAO rolDAO;

    @Override
    public Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni) {
        Usuario nuevoUsuario = new Usuario();

        // 1. Limpieza y extracción del PRIMER apellido
        String nombreLimpio = nombre.toLowerCase().trim();
        // Dividimos por espacios y nos quedamos con la primera posición (primer
        // apellido)
        String primerApellido = apellidos.toLowerCase().trim().split(" ")[0];

        // 2. Generar Username Único con el primer apellido
        String username = generarUsernameUnico(nombreLimpio, primerApellido);
        nuevoUsuario.setUsername(username);

        // 3. Password por defecto (DNI)
        nuevoUsuario.setPasswordHash(dni);
        nuevoUsuario.setActivo(true);

        // 4. Asignar Rol PACIENTE
        Rol rolPaciente = rolDAO.findByNombre("PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error: El rol 'PACIENTE' no existe en la base de datos"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolPaciente);
        nuevoUsuario.setRoles(roles);

        return usuarioDAO.save(nuevoUsuario);
    }

    private String generarUsernameUnico(String nombre, String apellido) {
        // Intento 1: Primera letra nombre + primer apellido (lmariño)
        String intento = nombre.substring(0, 1) + apellido;
        if (!usuarioDAO.existsByUsername(intento))
            return intento;

        // Intento 2: Dos primeras letras + primer apellido (lumariño)
        if (nombre.length() >= 2) {
            intento = nombre.substring(0, 2) + apellido;
            if (!usuarioDAO.existsByUsername(intento))
                return intento;
        }

        // Intento 3: Tres primeras letras + primer apellido (laumariño)
        if (nombre.length() >= 3) {
            intento = nombre.substring(0, 3) + apellido;
            if (!usuarioDAO.existsByUsername(intento))
                return intento;
        }

        // Fallback: base + número incremental
        int contador = 1;
        String base = nombre.substring(0, 1) + apellido;
        while (usuarioDAO.existsByUsername(base + contador)) {
            contador++;
        }
        return base + contador;
    }
}