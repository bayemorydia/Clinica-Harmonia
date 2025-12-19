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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);

        users.setUsersByUsernameQuery(
                "SELECT username, password_hash, activo FROM usuario WHERE username = ?");

        // Añadimos 'ROLE_' al nombre del rol para que Spring lo reconozca correctamente
        users.setAuthoritiesByUsernameQuery(
                "SELECT u.username, CONCAT('ROLE_', r.nombre) FROM usuario u " +
                        "JOIN usuario_roles ur ON u.usuario_id = ur.usuario_id " +
                        "JOIN roles r ON ur.rol_id = r.id " +
                        "WHERE u.username = ?");

        return users;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/generate-admin", "/login", "/css/**", "/js/**").permitAll()
                        // Usamos hasRole('ADMINISTRADOR'), Spring buscará 'ROLE_ADMINISTRADOR'
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/pacientes/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .defaultSuccessUrl("/admin", true)
                        .permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}