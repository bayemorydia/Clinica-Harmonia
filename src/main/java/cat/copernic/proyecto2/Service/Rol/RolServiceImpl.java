package cat.copernic.proyecto2.Service.Rol;

import cat.copernic.proyecto2.DAO.JPA.RolDAO;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolServiceImpl implements RolService {

    @Autowired
    private RolDAO rolDAO;

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> listarRoles(Pageable pageable) {
        return rolDAO.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> buscarPorNombre(String query, Pageable pageable) {
        return rolDAO.findByNombreContainingIgnoreCase(query, pageable);
    }

    @Override
    @Transactional
    public void guardar(Rol rol) {
        boolean existe;
        if (rol.getId() == null) {
            existe = rolDAO.existsByNombreIgnoreCase(rol.getNombre());
        } else {
            existe = rolDAO.existsByNombreIgnoreCaseAndIdNot(rol.getNombre(), rol.getId());
        }
        if (existe) {
            throw new RuntimeException("El nombre del rol ya existe.");
        }
        rolDAO.save(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol buscarRol(Long id) {
        return rolDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id) {
        rolDAO.findById(id).ifPresent(rol -> {
            rol.setActivo(!rol.isActivo());
            rolDAO.save(rol);
        });
    }
}