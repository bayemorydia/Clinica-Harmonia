package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.DAO.MedicamentoDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Indica a Spring que esta es la capa de negocio
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired // Inyección de dependencia del DAO
    private MedicamentoDAO medicamentoDAO;

    @Override
    @Transactional(readOnly = true) // Solo lectura para optimizar rendimiento
    public List<Medicamento> listarMedicamentos() {
        return (List<Medicamento>) medicamentoDAO.findAll();
    }

    @Override
    @Transactional // Requiere transacción de escritura
    public void guardar(Medicamento medicamento) {
        medicamentoDAO.save(medicamento);
    }

    @Override
    @Transactional
    public void eliminar(Medicamento medicamento) {
        medicamentoDAO.delete(medicamento);
    }

    @Override
    @Transactional(readOnly = true)
    public Medicamento encontrarMedicamento(Medicamento medicamento) {
        // Buscamos por ID y devolvemos null si no existe
        return medicamentoDAO.findById(medicamento.getId()).orElse(null);
    }
}