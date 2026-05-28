package com.electrodostore.venta_service.integration.common;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Configuración global de seguridad para request salientes
 * creadas mediante clientes Feign
 * */
@Configuration
public class FeignSecurityConfig {

    /**
     * Intercepta requests salientes a los microservicios
     * integrados y propaga la identidad del usuario autenticado
     */
    @Bean
    public RequestInterceptor jwtPropagationInterceptor(){

        //Permite modificar la request construida por Feign
        return requestTemplate -> {
            //Obtiene la autenticación de la request actual
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            //Si la autenticación proviene de JWT, propagamos el token
            if(authentication instanceof JwtAuthenticationToken jwtAuth){
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header(
                        "Authorization",
                        "Bearer " + token
                );
            }
        };
    }
}
