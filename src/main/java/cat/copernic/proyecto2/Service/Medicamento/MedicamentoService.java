package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import java.util.List;

public interface MedicamentoService {
    
    public List<Medicamento> listarMedicamentos();
    public void guardar(Medicamento medicamento);
    public void eliminar(Medicamento medicamento);
    public Medicamento encontrarMedicamento(Medicamento medicamento);
    
    // NUEVOS MÉTODOS DE VALIDACIÓN
    public boolean existeNombre(String nombre, Long idActual);
    public boolean existeCodigo(String codigo, Long idActual);
}