package cat.copernic.proyecto2.DAO;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TratamientoDAO extends JpaRepository<Tratamiento, Long> {
    // Al extender de JpaRepository, ya tienes: save(), findAll(), findById(), deleteById(), etc.
}
