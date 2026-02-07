package com.electrodostore.venta_service.integration.cliente.client;

import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//Interfaz que va a describir cada end-point que se necesite del servicio cliente-service
@FeignClient(name = "cliente-service", //-> Mismo nombre con el que se registró en Eureka-server
        configuration = ClienteFeignConfig.class) //Definimos configuración para el feign que hace peticiones a cliente-service
public interface ClienteFeignClient {

    //Descripción del método que encuentra un Cliente por su id
    @GetMapping("/clientes/{clientId}")
    ClienteIntegrationDto foundCliente(@PathVariable Long clientId);
}
