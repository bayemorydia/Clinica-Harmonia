package cat.copernic.proyecto2.Service;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento; // Import de tu entidad
import java.util.List;

public interface MedicamentoService {
    
    public List<Medicamento> listarMedicamentos();
    public void guardar(Medicamento medicamento);
    public void eliminar(Medicamento medicamento);
    public Medicamento encontrarMedicamento(Medicamento medicamento);
}