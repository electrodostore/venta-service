package com.electrodostore.venta_service.integration.cliente;

import com.electrodostore.venta_service.exception.BusinessException;
import com.electrodostore.venta_service.integration.cliente.client.ClienteFeignClient;
import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import com.electrodostore.venta_service.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClienteIntegrationService {

    private final ClienteFeignClient clienteFeign;

    public ClienteIntegrationService(ClienteFeignClient clienteFeign) {
        this.clienteFeign = clienteFeign;
    }

    //Circuit Breaker para consulta de cliente
    @CircuitBreaker(name = "cliente-service", fallbackMethod = "fallbackClienteService")
    @Retry(name = "cliente-service")
    public ClienteIntegrationDto findCliente(Long clienteId) {
        return clienteFeign.foundCliente(clienteId);
    }

    public ClienteIntegrationDto fallbackClienteService(Long clienteId, Throwable ex) {

        //Propaga excepciones de dominio sin modificarlas
        if(ex instanceof BusinessException be){
            throw be;
        }

        //Indica si hay fallo de infraestructura
        log.warn("Fallback activado para clienteId={}", clienteId + " en cliente-service", ex);
        throw new ServiceUnavailableException("No se logró establecer la comunicación con cliente-service. Por favor intente más tarde");

    }
}
