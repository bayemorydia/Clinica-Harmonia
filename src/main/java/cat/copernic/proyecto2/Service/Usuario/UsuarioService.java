package cat.copernic.proyecto2.Service.Usuario;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    // Creación de perfiles
    Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni);

    Usuario crearUsuarioEmpleado(String nombre, String apellidos, String dni, String rolNombre);

    // Gestión administrativa
    Page<Usuario> listarUsuarios(Pageable pageable);

    Page<Usuario> buscarPorUsername(String query, Pageable pageable);

    Page<Usuario> listarPaginado(String query, Boolean activo, Pageable pageable);

    Usuario buscarUsuario(Long id);

    void guardar(Usuario usuario);

    void actualizarRolUsuario(Long usuarioId, String nuevoRolNombre);
}