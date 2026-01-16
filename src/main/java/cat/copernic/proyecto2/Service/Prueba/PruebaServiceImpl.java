package cat.copernic.proyecto2.Service.Prueba;

import cat.copernic.proyecto2.DAO.JPA.PruebaDAO;
import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PruebaServiceImpl implements PruebaService {

    private final PruebaDAO pruebaDAO;

    @Override
    @Transactional(readOnly = true)
    public List<PruebaMedica> listarPruebas() {
        return pruebaDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PruebaMedica> getPruebasPaginadas(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return pruebaDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional
    public void guardar(PruebaMedica prueba) throws Exception {
        Optional<PruebaMedica> existenteCod = pruebaDAO.findByCodigo(prueba.getCodigo());
        if (existenteCod.isPresent()
                && (prueba.getId() == null || !existenteCod.get().getId().equals(prueba.getId()))) {
            throw new Exception("El código de prueba ya existe.");
        }

        Optional<PruebaMedica> existenteNom = pruebaDAO.findByNombre(prueba.getNombre());
        if (existenteNom.isPresent()
                && (prueba.getId() == null || !existenteNom.get().getId().equals(prueba.getId()))) {
            throw new Exception("Ya existe una prueba con ese nombre.");
        }
        pruebaDAO.save(prueba);
    }

    @Override
    @Transactional
    public void eliminarLogica(Long id) {
        pruebaDAO.findById(id).ifPresent(p -> {
            p.setActivo(false);
            pruebaDAO.save(p);
        });
    }

    @Override
    @Transactional
    public void activarLogica(Long id) {
        pruebaDAO.findById(id).ifPresent(p -> {
            p.setActivo(true);
            pruebaDAO.save(p);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PruebaMedica encontrarPrueba(Long id) {
        return pruebaDAO.findById(id).orElse(null);
    }

    @Override
    public long countPruebasActivas() {
        return pruebaDAO.countByActivoTrue();
    }
}