package cat.copernic.proyecto2.Security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { //Encriptador de contraseñas con Bcrypt
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsService(DataSource dataSource) { //Conector con la base de datos
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);

        users.setUsersByUsernameQuery(//El primer método busca usuarios y el segundo sus roles
                "SELECT username, password_hash, activo FROM usuario WHERE username = ?");

        users.setAuthoritiesByUsernameQuery(
                "SELECT u.username, CONCAT('ROLE_', r.nombre) FROM usuario u " +
                        "JOIN usuario_roles ur ON u.usuario_id = ur.usuario_id " +
                        "JOIN roles r ON ur.rol_id = r.id " +
                        "WHERE u.username = ?");

        return users;
    }
    //Este método define las reglas de acceso URL por URL
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/password/**").permitAll()
                        .requestMatchers("/home").authenticated() // Cualquier logueado puede
                        // entrar aquí
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/medico/**").hasAnyRole("MEDICO", "ADMINISTRADOR")
                        .requestMatchers("/paciente/**").hasAnyRole("PACIENTE", "ADMINISTRADOR")
                        .requestMatchers("/RRHH/**").hasRole("RRHH")
                        .requestMatchers("/Pacientes/**").hasRole("PACIENTE")
                        .requestMatchers("/recepcion/**").hasAnyRole("RECEPCIONISTA", "ADMINISTRADOR")
                        .requestMatchers("/vacaciones/**").hasAnyRole("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA", "RRHH")
                        .requestMatchers("/empleados/nuevo", "/empleados/guardar", "/empleados/editar/**").hasAnyRole("ADMINISTRADOR", "RRHH")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login") // Asegúrate de tener un login.html o quita esta
                        // línea para usar el de defecto
                        .defaultSuccessUrl("/home", true)
                        .permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}