package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.DAO.JPA.MedicamentoDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoDAO medicamentoDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentos() {
        return medicamentoDAO.findAll();
    }

    @Override
    public Medicamento crearNuevo() {
        Medicamento m = new Medicamento();
        m.setActivo(true); // Valor por defecto de negocio
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Medicamento> getMedicamentosPaginados(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return medicamentoDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional
    public void guardar(Medicamento medicamento) {
        // Lógica de negocio: Validación de duplicidad
        if (medicamento.getId() == null) {
            if (existePorCodigo(medicamento.getCodigo())) {
                throw new RuntimeException("CODIGO_DUPLICADO");
            }
        } else {
            if (existePorCodigoYIdNo(medicamento.getCodigo(), medicamento.getId())) {
                throw new RuntimeException("CODIGO_DUPLICADO");
            }
        }
        medicamentoDAO.save(medicamento);
    }

    @Override
    @Transactional(readOnly = true)
    public Medicamento encontrarMedicamento(Long id) {
        return medicamentoDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void eliminarLogica(Long id) {
        medicamentoDAO.findById(id).ifPresent(m -> {
            m.setActivo(false);
            medicamentoDAO.save(m);
        });
    }

    @Override
    @Transactional
    public void activarLogica(Long id) {
        medicamentoDAO.findById(id).ifPresent(m -> {
            m.setActivo(true);
            medicamentoDAO.save(m);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countMedicamentosActivos() {
        return medicamentoDAO.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return medicamentoDAO.existsByCodigo(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigoYIdNo(String codigo, Long id) {
        Medicamento m = medicamentoDAO.findByCodigo(codigo);
        return m != null && !m.getId().equals(id);
    }
}