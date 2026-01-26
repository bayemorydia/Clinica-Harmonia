package cat.copernic.proyecto2.Controller;

import cat.copernic.proyecto2.Service.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/forgot")
    public String forgotPasswordForm() {
        return "Login/forgot-password";
    }

    @PostMapping("/forgot")
    public String forgotPasswordSubmit(@RequestParam String dni, Model model) {
        try {
            // El servicio valida si el DNI existe y si tiene un usuario vinculado
            usuarioService.validarSolicitudReset(dni);
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "Login/forgot-password";
        }
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String dni,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        try {
            // El servicio valida contraseñas, busca al usuario y actualiza el hash
            usuarioService.resetearPassword(dni, newPassword, confirmPassword);
            model.addAttribute("success", "Contraseña actualizada correctamente. Puedes iniciar sesión.");
            return "Login/Login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("dni", dni);
            return "Login/reset-password";
        }
    }
}