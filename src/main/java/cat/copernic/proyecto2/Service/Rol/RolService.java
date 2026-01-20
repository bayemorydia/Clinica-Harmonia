package cat.copernic.proyecto2.Service.Rol;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RolService {
    Page<Rol> listarRoles(Pageable pageable);
    void guardar(Rol rol);
    Rol encontrarRol(Long id); // Nombre normalizado para el controlador
    void desactivar(Long id);  // Nombre normalizado para el controlador
    void activar(Long id);    // Nombre normalizado para el controlador
    Page<Rol> getRolesPaginados(String query, Boolean activo, int page, int size);
    List RolesConcretos();
}