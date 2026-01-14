package cat.copernic.proyecto2.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        // Si el usuario ya está autenticado, que vaya a home, si no, al login
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String redirectAfterLogin(HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMINISTRADOR")) {
            return "redirect:/admin";
        } else if (request.isUserInRole("ROLE_MEDICO")) {
            return "redirect:/medico/dashboard";
        } else if (request.isUserInRole("ROLE_RRHH")) {
            return "redirect:/RRHH/dashboard";
        }

        // ¡OJO! No redirijas a /login aquí.
        // Si llega aquí y no tiene roles, mándalo a una página de "acceso denegado"
        // o a una página de inicio pública.
        return "redirect:/login?error=no-role";
    }

}
