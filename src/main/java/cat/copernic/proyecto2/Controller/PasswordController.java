package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Domain.Administración.Empleado;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import cat.copernic.proyecto2.Service.Empleado.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot")
    public String forgotPasswordForm() {
        return "Login/forgot-password";
    }

    @PostMapping("/forgot")
    public String forgotPasswordSubmit(@RequestParam String dni, Model model) {
        try {
            Empleado empleado = empleadoService.obtenerPorDni(dni);
            if (empleado == null) {
                model.addAttribute("error", "DNI/NIE no encontrado");
                return "Login/forgot-password";
            }

            Usuario usuario = empleado.getUsuario();
            if (usuario == null) {
                model.addAttribute("error", "El empleado no tiene usuario asociado");
                return "Login/forgot-password";
            }
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar el documento");
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

            // Encriptamos con el passwordEncoder inyectado y guardamos a través del
            // UsuarioService
            usuario.setPasswordHash(passwordEncoder.encode(newPassword));
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