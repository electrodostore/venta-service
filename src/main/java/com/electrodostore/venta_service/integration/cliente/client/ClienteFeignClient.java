package com.electrodostore.venta_service.integration.cliente.client;

import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign que hace peticiones a cliente-service
 * */
@FeignClient(name = "cliente-service",
        configuration = ClienteFeignConfig.class)
public interface ClienteFeignClient {

    @GetMapping("/clientes/{clientId}/enabled")
    ClienteIntegrationDto foundCliente(@PathVariable Long clientId);
}
