package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface NominaDAO extends JpaRepository<Nomina, Long> {
    // Aquí puedes añadir consultas personalizadas si las necesitas

    // Devuelve true si existe alguna nómina para el empleado y mes/año del periodo
    boolean existsByEmpleadoIdAndPeriodo(Long empleadoId, String periodo);

    Page<Nomina> findByEmpleadoId(Long empleadoId, Pageable pageable);

    List<Nomina> findByPeriodo(String periodo);

    List<Nomina> findByPeriodoContaining(String anyo);
}
