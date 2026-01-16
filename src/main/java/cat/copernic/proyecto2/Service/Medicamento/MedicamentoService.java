package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import org.springframework.data.domain.Page;
import java.util.List;

public interface MedicamentoService {
    List<Medicamento> listarMedicamentos();

    Page<Medicamento> getMedicamentosPaginados(String query, Boolean activo, int page, int size);

    void guardar(Medicamento medicamento);

    Medicamento encontrarMedicamento(Long id);

    void eliminarLogica(Long id);

    void activarLogica(Long id);

    long countMedicamentosActivos();
}