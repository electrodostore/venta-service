package com.electrodostore.venta_service.integration.producto.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para el cliente Feign que
 * hace peticiones a producto-service
 */
@Configuration
public class ProductoFeignConfig {

    /**
     * Configura ErrorDecoder para interpretar
     * y transformar responses de error provenientes
     * de producto-service.
     * */
    @Bean
    public ProductoErrorDecoder productoErrorDecoder(){
        return new ProductoErrorDecoder();
    }
}
