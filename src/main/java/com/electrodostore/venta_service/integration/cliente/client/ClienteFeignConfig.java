package com.electrodostore.venta_service.integration.cliente.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClienteFeignConfig {

    /*Decodifica errorCodes en respuestas de error provenientes de cliente-service*/
    @Bean
    public ClienteErrorDecoder clienteErrorDecoder(){
        return new ClienteErrorDecoder();
    }
}
