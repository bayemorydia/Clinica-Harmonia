package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import java.util.List;

public interface MedicamentoService {
    
    List<Medicamento> listarMedicamentos();
    void guardar(Medicamento medicamento);
    void eliminar(Medicamento medicamento);
    Medicamento encontrarMedicamento(Medicamento medicamento);
    boolean existeNombre(String nombre, Long idActual);
    boolean existeCodigo(String codigo, Long idActual);
    long countMedicamentosActivos();
}