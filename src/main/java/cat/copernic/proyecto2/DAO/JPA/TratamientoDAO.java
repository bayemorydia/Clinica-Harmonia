package cat.copernic.proyecto2.DAO.JPA;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TratamientoDAO extends JpaRepository<Tratamiento, Long> {
    
    // Método estándar: Busca un tratamiento por su nombre exacto
    Tratamiento findByNombre(String nombre);
}