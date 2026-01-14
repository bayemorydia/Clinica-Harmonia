package cat.copernic.proyecto2.Service.Empleado;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Rol;
import org.springframework.data.domain.Page;
import cat.copernic.proyecto2.Domain.Administración.Nomina;
import java.util.List;

public interface EmpleadoService {
    Empleado crear(Empleado empleado);
    Empleado actualizar(Long id, Empleado datos);
    void desactivarEmpleado(Long id);
    Empleado obtenerPorUsername(String username);
    Empleado obtenerPorId(Long id);
    List<Empleado> listarTodos();
    List<Empleado> listarActivos();
    List<Empleado> buscarPorRol(Rol rol);
    List<Empleado> buscarPorDepartamento(String departamento);
    List<Empleado> buscarPorNombreOApellidos(String termino);
    long ContarEmpleados();
    Page<Nomina> listarNominasPorEmpleado(Long empleadoId, String search, int page, int size);
}