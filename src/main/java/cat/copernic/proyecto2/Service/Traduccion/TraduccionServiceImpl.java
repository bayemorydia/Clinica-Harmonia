package cat.copernic.proyecto2.Service.Traduccion;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import cat.copernic.proyecto2.DAO.TraduccionDAO;

@Service
public class TraduccionServiceImpl implements TraduccionService {

    @Autowired
    private TraduccionDAO traduccionDAO;

    @Override
public String getTraduccion(String key, String lang) {
    return traduccionDAO.findById(key)
        .map(t -> {
            switch (lang.toLowerCase()) {
                case "ca": return t.getCa();
                case "en": return t.getEn();
                default: return t.getEs();
            }
        })
        .orElse(key);
}

    public String getTraduccion(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return getTraduccion(key, locale.getLanguage());
    }
}
