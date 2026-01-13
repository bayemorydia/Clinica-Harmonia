package cat.copernic.proyecto2.Service.Medicamento;

import cat.copernic.proyecto2.DAO.MedicamentoDAO;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired 
    private MedicamentoDAO medicamentoDAO;

    @Override
    @Transactional(readOnly = true) 
    public List<Medicamento> listarMedicamentos() {
        return (List<Medicamento>) medicamentoDAO.findAll();
    }

    @Override
    @Transactional 
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
        return medicamentoDAO.findById(medicamento.getId()).orElse(null);
    }

    // --- NUEVA LÓGICA DE VALIDACIÓN ---

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre, Long idActual) {
        Medicamento encontrado = medicamentoDAO.findByNombre(nombre);
        
        // Si no existe, perfecto
        if (encontrado == null) {
            return false; 
        }
        
        // Si existe, comprobamos si es el mismo que estamos editando
        if (idActual != null && encontrado.getId().equals(idActual)) {
            return false; // Es el mismo, no cuenta como duplicado
        }
        
        return true; // Es otro registro distinto -> DUPLICADO
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo, Long idActual) {
        Medicamento encontrado = medicamentoDAO.findByCodigo(codigo);
        
        if (encontrado == null) {
            return false;
        }
        
        if (idActual != null && encontrado.getId().equals(idActual)) {
            return false;
        }
        
        return true;
    }
}