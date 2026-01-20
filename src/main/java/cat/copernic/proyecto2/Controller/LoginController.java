package cat.copernic.proyecto2.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String redirectAfterLogin(HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMINISTRADOR")) {
            return "redirect:/admin";
        } else if (request.isUserInRole("ROLE_MEDICO")) {
            return "redirect:/medico/dashboard";
        } else if (request.isUserInRole("ROLE_PACIENTE")){
            return "redirect:/paciente/dashboard";
        } else if (request.isUserInRole("ROLE_RECEPCIONISTA")) {
            return "redirect:/recepcion/dashboard";
        }else if (request.isUserInRole("ROLE_RRHH")) {
            return "redirect:/RRHH/dashboard";
        }
        return "redirect:/login?error=no-role";
    }
    
    @GetMapping("/login")
    public String login() {
        return "/Login/Login"; // busca templates/login.html
    }
}
