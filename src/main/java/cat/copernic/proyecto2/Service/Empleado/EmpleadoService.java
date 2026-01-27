package cat.copernic.proyecto2.Service.Empleado;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface EmpleadoService {
    Empleado crear(Empleado empleado) throws Exception;

    Empleado actualizar(Long id, Empleado datos) throws Exception;
    Map<String, Long> obtenerEstadisticasDashboard();

    void desactivarEmpleado(Long id);
    void activarEmpleado(Long id);

    Empleado obtenerPorUsername(String username);

    Empleado obtenerPorId(Long id);

    List<Empleado> listarTodos();

    Page<Empleado> getEmpleadosPaginados(String query, Boolean activo, int page, int size);

    long ContarEmpleados();

    Empleado obtenerPorDni(String dni);

}