package cat.copernic.proyecto2.DAO;
import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoDAO extends JpaRepository<Medicamento, Long> { 
    
    // Spring Data genera las consultas automáticamente basándose en el nombre
    Medicamento findByNombre(String nombre);
    Medicamento findByCodigo(String codigo);
}