package cat.copernic.proyecto2.Service.Prueba;

import cat.copernic.proyecto2.Domain.PersonalSanitario.PruebaMedica;
import org.springframework.data.domain.Page;
import java.util.List;

public interface PruebaService {
    List<PruebaMedica> listarPruebas();
    Page<PruebaMedica> getPruebasPaginadas(String query, Boolean activo, int page, int size);
    void guardar(PruebaMedica prueba) throws Exception;
    void eliminarLogica(Long id);
    void activarLogica(Long id);
    PruebaMedica encontrarPrueba(Long id);
    long countPruebasActivas();
    
    // Nuevo método para inicialización
    PruebaMedica crearNuevaPrueba();
}