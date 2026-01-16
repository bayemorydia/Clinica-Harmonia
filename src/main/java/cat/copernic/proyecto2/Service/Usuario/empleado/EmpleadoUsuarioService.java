package cat.copernic.proyecto2.Service.Usuario.empleado;

import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmpleadoUsuarioService {
    Usuario crearEmpleadoConUsuario(String nombre, String apellidos, String dni, String rolNombre) ;
    // Métodos para la gestión administrativa
    Page<Usuario> listarUsuarios(Pageable pageable);
    Page<Usuario> buscarPorUsername(String query, Pageable pageable);
    Usuario buscarUsuario(Long id);
    void guardar(Usuario usuario);
}

