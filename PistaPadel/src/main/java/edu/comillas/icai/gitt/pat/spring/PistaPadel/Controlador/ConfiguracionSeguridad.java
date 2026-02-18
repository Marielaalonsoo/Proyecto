package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {

        // Tanto register como health sin autenticación
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/pistaPadel/health", "/pistaPadel/auth/register").permitAll()
                .anyRequest().authenticated()
        );

        // http.formLogin(Customizer.withDefaults());  No redirige (302), sin esto acceso con Basic Auth
        // y si no estás autentucado 401 Unauthorized (solo entrega 1 en principio)
        http.httpBasic(Customizer.withDefaults());

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/pistaPadel/**"));

        return http.build();
    }

    @Bean
    public UserDetailsService usuarios() {

        // noop sirve para decirle a Spring que la contraseña no está encriptada, es literalmente lo que pone
        // withDefaultPasswordEncoder() nos daba warning, se lo tragaba pero warning
        UserDetails user = User.withUsername("user")
                .password("{noop}user")
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}
