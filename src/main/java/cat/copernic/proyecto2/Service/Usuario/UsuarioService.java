package cat.copernic.proyecto2.Service.Usuario;

import cat.copernic.proyecto2.Domain.Persona.Usuario;

public interface UsuarioService {
    Usuario crearUsuarioPaciente(String nombre, String apellidos, String dni);
}
