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
                                                .requestMatchers("/login", "/css/**", "/js/**", "/img/**").permitAll()
                                                .requestMatchers("/home").authenticated() // Cualquier logueado puede
                                                                                          // entrar aquí
                                                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers("/medico/**").hasAnyRole("MEDICO", "ADMINISTRADOR")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                //.loginPage("/login") // Asegúrate de tener un login.html o quita esta
                                                                     // línea para usar el de defecto
                                                .defaultSuccessUrl("/home", true)
                                                .permitAll())
                                .logout(logout -> logout.permitAll());

                return http.build();
        }
}