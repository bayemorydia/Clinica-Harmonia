package cat.copernic.proyecto2.DAO;
import cat.copernic.proyecto2.Domain.Administración.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TurnoDAO extends JpaRepository<Turno, Long> {
    Optional<Turno> findByNombre(String nombre);


}