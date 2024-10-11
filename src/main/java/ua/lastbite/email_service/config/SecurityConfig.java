package ua.lastbite.email_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Turning off CSRF for simple testing
        http.csrf(AbstractHttpConfigurer::disable);

        // Customize the permissions of all requests
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
        );

        // Turning off the login form
        http.formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}