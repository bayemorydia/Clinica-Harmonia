package cat.copernic.proyecto2.Service.Usuario.Proveedor;

import cat.copernic.proyecto2.DAO.JPA.RolDAO;
import cat.copernic.proyecto2.DAO.JPA.UsuarioDAO;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UsuarioProveedorServiceImpl implements UsuarioProveedorService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private RolDAO rolDAO;

    @Override
    public Usuario crearUsuarioProveedor(String nombre, String cif) {
        Usuario nuevoUsuario = new Usuario();

        // 1️⃣ Limpiar nombre completo
        String nombreLimpio = nombre.toLowerCase().trim().replaceAll("\\s+", "");

        // 2️⃣ Generar username único usando el nombre completo
        String username = generarUsernameUnico(nombreLimpio);
        nuevoUsuario.setUsername(username);

        // 3️⃣ Password por defecto = CIF
        nuevoUsuario.setPasswordHash(cif);
        nuevoUsuario.setActivo(true);

        // 4️⃣ Asignar rol PROVEEDOR
        Rol rolProveedor = rolDAO.findByNombre("PROVEEDOR")
                .orElseThrow(() -> new RuntimeException("Error: El rol 'PROVEEDOR' no existe en la base de datos"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolProveedor);
        nuevoUsuario.setRoles(roles);

        return usuarioDAO.save(nuevoUsuario);
    }

    private String generarUsernameUnico(String nombreLimpio) {
        // Intento 1: usar el nombre completo
        String intento = nombreLimpio;
        if (!usuarioDAO.existsByUsername(intento))
            return intento;

        // Fallback: añadir número incremental si ya existe
        int contador = 1;
        while (usuarioDAO.existsByUsername(intento + contador)) {
            contador++;
        }
        return intento + contador;
    }
}
