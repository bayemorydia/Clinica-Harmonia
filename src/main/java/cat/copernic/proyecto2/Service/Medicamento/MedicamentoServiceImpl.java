package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.DAO.JPA.MedicamentoDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Transactional(readOnly = true)
    public Page<Medicamento> getMedicamentosPaginados(String query, Boolean activo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return medicamentoDAO.findByFilters(query, activo, pageable);
    }

    @Override
    @Transactional
    public void guardar(Medicamento medicamento) {
        // Validación: solo obligamos precio > 0 si el producto se guarda como ACTIVO
        if (medicamento.isActivo()) {
            if (medicamento.getPrecioUnitario() == null || medicamento.getPrecioUnitario() <= 0) {
                throw new IllegalArgumentException(
                        "El precio del medicamento debe ser mayor que cero para estar activo.");
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
            if (m.getPrecioUnitario() == null || m.getPrecioUnitario() <= 0) {
                throw new RuntimeException("No se puede activar un producto sin un precio válido.");
            }
            m.setActivo(true);
            medicamentoDAO.save(m);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countMedicamentosActivos() {
        return medicamentoDAO.countByActivoTrue();
    }
}