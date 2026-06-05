package com.electrodostore.venta_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                        oauth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(
                                        //Configura convertidor para extraer las autoridades desde el JWT.
                                        jwtAuthenticationConverter()
                                )
                        )
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .build();
    }

    /**
     * Configura el mecanismo que transforma las autoridades
     * almacenadas dentro del JWT en objetos GrantedAuthority
     * que Spring Security utiliza durante la autorización.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        /*Componente encargado de leer una lista de autoridades
         * desde un claim específico del JWT.*/
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        /*Indica que las autoridades deben obtenerse
         * del claim "authorities" y evita que Spring agregue algún prefijo.*/
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        /*Convertidor principal usado por el Resource Server
         * para construir el objeto Authentication a partir
         * del JWT validado.*/
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        /*Registra el convertidor encargado de extraer
         * las autoridades desde el JWT.*/
        converter.setJwtGrantedAuthoritiesConverter(
                grantedAuthoritiesConverter
        );

        return converter;
    }
}