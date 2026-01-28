package cat.copernic.proyecto2.Service.Rol;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import java.util.List;

public interface RolService {
    List<Rol> listarTodos();
    void guardar(Rol rol);
    Rol encontrarRol(Long id);
    void desactivar(Long id);
    void activar(Long id);
    Page<Rol> getRolesPaginados(String query, Boolean activo, int page, int size);
    List<Rol> RolesConcretos();
    Rol prepararNuevoRol();
}