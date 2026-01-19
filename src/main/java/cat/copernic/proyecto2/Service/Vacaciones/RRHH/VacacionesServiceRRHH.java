package cat.copernic.proyecto2.Service.Vacaciones.RRHH;

import cat.copernic.proyecto2.Domain.Administración.Vacaciones;

import java.util.List;

public interface VacacionesServiceRRHH {
    List<Vacaciones> listarTodasLasVacaciones();
    Vacaciones obtenerPorId(Long id);
    Vacaciones aprobarVacaciones(Long id);
    Vacaciones denegarVacaciones(Long id);
    long contarPendientes();
}
