package cat.copernic.proyecto2.Domain.Traduccion;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "traducciones")
@Data
public class Traduccion {
    @Id
    private String id;
    private String es;
    private String ca;
    private String en;
}
