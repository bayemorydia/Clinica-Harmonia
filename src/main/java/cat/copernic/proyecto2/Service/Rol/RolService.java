package cat.copernic.proyecto2.Service.Rol;

import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RolService {
    Page<Rol> listarRoles(Pageable pageable);
    Page<Rol> buscarPorNombre(String query, Pageable pageable);
    void guardar(Rol rol);
    Rol buscarRol(Long id);
    void cambiarEstado(Long id); // Para activar/desactivar
}
