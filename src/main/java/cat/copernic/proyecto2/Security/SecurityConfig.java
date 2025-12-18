package cat.copernic.proyecto2.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        PasswordEncoder encoder = passwordEncoder();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso a recursos estáticos (CSS, JS, Imágenes)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        
                        // Flujo de Pacientes: Solo ADMIN
                        .requestMatchers("/pacientes/**").hasRole("ADMIN")
                        
                        // Flujo de Citas: Solo ADMIN
                        .requestMatchers("/citas/**").hasRole("ADMIN")

                        // Flujo de Proveedores: Solo ADMIN
                        .requestMatchers("/proveedores/**").hasRole("ADMIN")
                        
                        // Cualquier otra petición requiere estar autenticado
                        .anyRequest().authenticated())
                
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true) // Redirige al inicio tras login
                        .permitAll())
                
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // Redirige a login tras cerrar sesión
                        .permitAll());
        
        return http.build();
    }
}