package cat.copernic.proyecto2.Service.Diagnostico;

import cat.copernic.proyecto2.DAO.JPA.DiagnosticoDAO;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Diagnostico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DiagnosticoServiceImpl implements DiagnosticoService {

    @Autowired
    private DiagnosticoDAO diagnosticoDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Diagnostico> listarTodos() {
        return diagnosticoDAO.findAll();
    }

    @Override
    public Diagnostico crearNuevoDiagnostico() {
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setActivo(true); // Valor por defecto de negocio
        return diagnostico;
    }

    @Override
    @Transactional(readOnly = true)
    public Diagnostico obtenerPorIdOCrash(Long id) throws Exception {
        return diagnosticoDAO.findById(id)
                .orElseThrow(() -> new Exception("Diagnóstico no encontrado."));
    }

    @Override
    @Transactional
    public void guardar(Diagnostico diagnostico) throws Exception {
        // Validaciones de duplicados al crear (id nulo)
        if (diagnostico.getId() == null) {
            if (diagnosticoDAO.existsByCodigo(diagnostico.getCodigo())) {
                throw new Exception("El código " + diagnostico.getCodigo() + " ya existe.");
            }
            if (diagnosticoDAO.existsByNombre(diagnostico.getNombre())) {
                throw new Exception("El diagnóstico " + diagnostico.getNombre() + " ya existe.");
            }
        } else {
            // Validaciones al editar (comprobar que no choque con otros IDs)
            // Buscamos el diagnóstico existente en base al ID
            Diagnostico existente = diagnosticoDAO.findById(diagnostico.getId()).orElse(null);
            // Si el código ha cambiado y ya existe en otro diagnóstico, lanza excepción
            if (existente != null) {
                if (!existente.getCodigo().equals(diagnostico.getCodigo())
                        && diagnosticoDAO.existsByCodigo(diagnostico.getCodigo())) {
                    throw new Exception("El código ya está en uso por otro diagnóstico.");
                }
            }
        }

        diagnosticoDAO.save(diagnostico);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Diagnostico> buscarPorId(Long id) {
        return diagnosticoDAO.findById(id);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        diagnosticoDAO.findById(id).ifPresent(d -> {
            d.setActivo(false);
            diagnosticoDAO.save(d);
        });
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        diagnosticoDAO.findById(id).ifPresent(d -> {
            d.setActivo(true);
            diagnosticoDAO.save(d);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Diagnostico> getDiagnosticosPaginados(String keyword, Boolean activo, int page, int size) {
        return diagnosticoDAO.findByFilters(keyword, activo, PageRequest.of(page, size));
    }

    @Override
    public long countDiagnosticosActivos() {
        return diagnosticoDAO.countByActivoTrue();
    }
}