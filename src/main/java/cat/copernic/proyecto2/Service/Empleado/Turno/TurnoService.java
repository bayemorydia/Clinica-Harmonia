package cat.copernic.proyecto2.Service.Empleado.Turno;
import cat.copernic.proyecto2.Domain.Administración.Turno;

import java.util.List;

public interface TurnoService  {

    void CrearTurnos();

    List<Turno> listarTodos();
}
