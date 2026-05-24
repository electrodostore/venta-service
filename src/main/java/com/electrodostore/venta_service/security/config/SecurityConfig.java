package com.electrodostore.venta_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define la cadena de filtros de Spring Security
     * encargada de proteger las requests del microservicio.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())

                /**
                 * Configura el microservicio como OAuth2 Resource Server
                 * para validar automáticamente tokens JWT.
                 */
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )

                /**
                 * El microservicio no almacena sesiones.
                 * Cada request debe autenticarse mediante JWT.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /**
                 * Toda request requiere autenticación,
                 * excepto las rutas públicas que se definan explícitamente.
                 */
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()
                )

                .build();
    }
}