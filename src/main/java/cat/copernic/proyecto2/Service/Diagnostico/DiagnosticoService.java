package cat.copernic.proyecto2.Service.Diagnostico;

import cat.copernic.proyecto2.Domain.PersonalSanitario.Diagnostico;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface DiagnosticoService {

    List<Diagnostico> listarTodos();

    void guardar(Diagnostico diagnostico) throws Exception; // Exception para validaciones

    Optional<Diagnostico> buscarPorId(Long id);
    long countDiagnosticosActivos();
    void desactivar(Long id); // Borrado lógico

    void reactivar(Long id);

    Page<Diagnostico> getDiagnosticosPaginados(String keyword, Boolean activo, int page, int size);
}
