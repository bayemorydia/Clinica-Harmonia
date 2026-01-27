package cat.copernic.proyecto2.Service.Rol;

import cat.copernic.proyecto2.DAO.JPA.RolDAO;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolDAO rolDAO;

    @Override
    public Rol prepararNuevoRol() {
        Rol rol = new Rol();
        rol.setActivo(true);
        return rol;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> listarRoles(Pageable pageable) {
        return rolDAO.findAll(pageable);
    }

    @Override
    @Transactional
    public void guardar(Rol rol) {
        Rol existente = rolDAO.findByNombreIgnoreCase(rol.getNombre()).orElse(null);
        if (existente != null) {
            if (rol.getId() == null || !existente.getId().equals(rol.getId())) {
                throw new RuntimeException("Ya existe un rol con el nombre: " + rol.getNombre());
            }
        }
        rolDAO.save(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol encontrarRol(Long id) {
        return rolDAO.findById(id).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        rolDAO.findById(id).ifPresent(rol -> {
            rol.setActivo(false);
            rolDAO.save(rol);
        });
    }

    @Override
    @Transactional
    public void activar(Long id) {
        rolDAO.findById(id).ifPresent(rol -> {
            rol.setActivo(true);
            rolDAO.save(rol);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> getRolesPaginados(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return rolDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> RolesConcretos() {
        return rolDAO.findRolesEspecificos();
    }
}