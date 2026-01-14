package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Farmacia.Medicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoDAO extends JpaRepository<Medicamento, Long> { 
    Medicamento findByNombre(String nombre);
    Medicamento findByCodigo(String codigo);
    List<Medicamento> findByNombreContainingIgnoreCase(String nombre);
    long countByActivoTrue();
}
