package cat.copernic.proyecto2.Service.Traduccion;

import org.springframework.stereotype.Service;

@Service
public interface TraduccionService {
    String getTraduccion(String key, String lang);
    String getTraduccion(String key);
}
