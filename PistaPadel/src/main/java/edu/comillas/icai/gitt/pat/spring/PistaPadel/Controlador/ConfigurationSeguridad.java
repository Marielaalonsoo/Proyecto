package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class ConfigurationSeguridad {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user").password("user").roles("USER").build();

        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin").password("admin").roles("ADMIN").build();

        return new InMemoryUserDetailsManager(user, admin);
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
//                .httpBasic(basic -> {});
//        return http.build();
//    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()   )
            .httpBasic(basic -> {});
    return http.build();
}

}
