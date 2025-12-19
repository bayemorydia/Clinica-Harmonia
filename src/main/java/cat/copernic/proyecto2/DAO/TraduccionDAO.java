package cat.copernic.proyecto2.DAO;
import org.springframework.data.mongodb.repository.MongoRepository;
import cat.copernic.proyecto2.Domain.Traduccion.Traduccion;

public interface TraduccionDAO extends MongoRepository<Traduccion, String> {

}
