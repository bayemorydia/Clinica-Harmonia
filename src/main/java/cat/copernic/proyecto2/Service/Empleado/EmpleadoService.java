package cat.copernic.proyecto2.Service.Empleado;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.Page;
import java.util.List;

public interface EmpleadoService {
    Empleado crear(Empleado empleado);

    Empleado actualizar(Long id, Empleado datos);

    void desactivarEmpleado(Long id);
    void activarEmpleado(Long id);

    Empleado obtenerPorUsername(String username);

    Empleado obtenerPorId(Long id);

    List<Empleado> listarTodos();

    Page<Empleado> getEmpleadosPaginados(String query, Boolean activo, int page, int size);

    long ContarEmpleados();

    Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size);
}