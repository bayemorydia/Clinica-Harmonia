package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Usuario.empleado.EmpleadoUsuarioServiceImpl;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private EmpleadoServiceImpl empleadoService;

    @Autowired
    private EmpleadoUsuarioServiceImpl usuarioService;

    @GetMapping("/forgot")
    public String forgotPasswordForm() {
        return "Login/forgot-password";
    }

    @PostMapping("/forgot")
    public String forgotPasswordSubmit(@RequestParam String dni, Model model) {
        try {
            Empleado empleado = empleadoService.obtenerPorDni(dni);
            Usuario usuario = empleado.getUsuario();
            if (usuario == null) {
                model.addAttribute("error", "El empleado no tiene usuario asociado");
                return "Login/forgot-password";
            }
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        } catch (Exception e) {
            model.addAttribute("error", "DNI/NIE no encontrado");
            return "Login/forgot-password";
        }
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String dni,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        }
        try {
            Empleado empleado = empleadoService.obtenerPorDni(dni);
            Usuario usuario = empleado.getUsuario();
            usuario.setPasswordHash(usuarioService.getPasswordEncoder().encode(newPassword));
            usuarioService.guardar(usuario);
            model.addAttribute("success", "Contraseña actualizada correctamente. Puedes iniciar sesión.");
            return "Login/Login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar la contraseña");
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        }
    }
}
