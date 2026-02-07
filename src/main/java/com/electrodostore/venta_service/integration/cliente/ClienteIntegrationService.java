package com.electrodostore.venta_service.integration.cliente;

import com.electrodostore.venta_service.integration.cliente.client.ClienteFeignClient;
import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import com.electrodostore.venta_service.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//@Slf4j contiene un logger para notificar errores y eventos en el log del proyecto
@Slf4j
@Service
public class ClienteIntegrationService {

    //Inyección de dependencia por constructor para el FeignClient del servicio Cliente
    private final ClienteFeignClient clienteFeign;

    public ClienteIntegrationService(ClienteFeignClient clienteFeign) {
        this.clienteFeign = clienteFeign;
    }

    //Protección por Circuit-Breaker del método que integra el servicio Cliente con el servicio Venta para buscar un cliente pedido desde la veta
    //Error de algún tipo en la comunicación -> "plan-B" = fallbackMethod
    //@Retry = reintentar una cierta contidad de veces antes de ir al fallback
    @CircuitBreaker(name = "cliente-service", fallbackMethod = "fallbackClienteService")
    @Retry(name = "cliente-service")
    public ClienteIntegrationDto findCliente(Long clienteId) {
        return clienteFeign.foundCliente(clienteId);
    }

    //El fallback debe tener la misma firma del método protegido, además del último parámetro Throwable
    public ClienteIntegrationDto fallbackClienteService(Long clienteId, Throwable ex) {

        //Lanzamos el warning al log del proyecto indicando la activación del fallback
        log.warn("Fallback activado para clienteId={}", clienteId + " en cliente-service", ex);

        //Excepción de infraestructura en la comunicación de los servicios -> ServiceUnavailable
        throw new ServiceUnavailableException("No se logró establecer la comunicación con cliente-service. Por favor intente más tarde");

    }
}
