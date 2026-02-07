package com.electrodostore.venta_service.integration.cliente.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Clase de configuración para el FeignClient que hace peticiones a cliente-service
@Configuration
public class ClienteFeignConfig {

    //Registramos el Bean del decodificador de errorCodes para cliente-service
    /*Cuando cliente-service envía una response con statusCode diferente a 2xx, antes de construirse el FeignException a partir
     de esa response, Feign le da un vistazo a la clase ClienteErrorDecoder para ver si esa excepción se puede transformar
     en una excepción de dominio*/
    @Bean
    public ClienteErrorDecoder clienteErrorDecoder(){
        return new ClienteErrorDecoder();
    }
}
