package cat.copernic.proyecto2.Service.Usuario;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni);
    Usuario crearUsuarioEmpleado(String nombre, String apellidos, String dni, String rolNombre);
    void actualizarRolUsuario(Long usuarioId, String nuevoRolNombre);
    Page<Usuario> listarPaginado(String query, Boolean activo, Pageable pageable);
    Usuario buscarUsuario(Long id);
    void validarSolicitudReset(String dni) throws Exception;
    Empleado obtenerEmpleadoPorUsername(String username);
    void resetearPassword(String dni, String newPassword, String confirmPassword) throws Exception;
    void activarUsuario(Long id);
    void desactivarUsuario(Long id);
}