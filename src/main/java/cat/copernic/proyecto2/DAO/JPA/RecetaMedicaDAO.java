package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.RecetaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaMedicaDAO extends JpaRepository<RecetaMedica, Long> {
}
