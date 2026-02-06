package com.electrodostore.venta_service.integration.producto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Clase de configuración para el Feign que hace peticiones a producto-service
@Configuration
public class ProductoFeignConfig {

    //Guardamos el Bean en el contenedor de Spring del errorDecoder de producto-service
    /*Esto permitirá que antes de que Feign construya una excepción a partir de la Response con statusCode diferente a 2xx
     eche un vistazo al errorDecoder de Producto para ver si este la puede transformar a excepción de dominio*/
    @Bean
    public ProductoErrorDecoder registrarErrorDecoder(ObjectMapper objectMapper){
        return new ProductoErrorDecoder();
    }
}
