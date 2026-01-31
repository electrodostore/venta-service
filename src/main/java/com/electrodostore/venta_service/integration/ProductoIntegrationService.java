package com.electrodostore.venta_service.integration;

import com.electrodostore.venta_service.client.ProductoClient;
import com.electrodostore.venta_service.dto.ProductoIntegrationDto;
import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//Slf4j provee un logger para establecer eventos y errores en el log del proyecto
@Slf4j
@Service
public class ProductoIntegrationService {

    //Inyección de dependencia por constructor para el cliente de producto-service
    private final ProductoClient productoClient;
    public ProductoIntegrationService(ProductoClient productoClient){
        this.productoClient = productoClient;
    }

    //Método protegido con Circuit-Breaker para acceder como cliente al servicio producto
    //Si hay algún problema en la comunicación con producto-service o si el circuito está: open -> fallbackMethod
    //El fallbackMethod sirve como plan-B en caso de alguna excepción en la ejecución de este método
    /*En caso de error el @Retry reintenta la petición un determinado número de veces para no ir directamente al
    fallbackMethod sino darle como otras oportunidades a la petición de realizarse*/
    @CircuitBreaker(name = "producto-service-read", fallbackMethod = "fallbackFindProductos")
    @Retry(name = "producto-service-read")
    public List<ProductoIntegrationDto> findProductos(Set<Long> productsIds){
        return productoClient.findProductos(new ArrayList<>(productsIds));
    }

    //Definimos fallbackMethod que tiene que tener la misma firma que el método protegido
    //Además su último parámetro debe ser un objeto Throwable que es básicamente la excepción que lo provocó
    public List<ProductoIntegrationDto> fallbackFindProductos(Set<Long> productsIds, Throwable ex){

        //Agregamos le warn al log indicando que el fallback fue activado por problema de comunicación
        log.warn("fallback activado para producto-service", ex);

        //Si la excepción es NOT_FOUND, ya se sabe que es porque no se encontró ningún producto, entonces lo indicamos
        if(ex instanceof FeignException.NotFound){
            throw new ProductoNotFoundException("No se encontró ningún producto solicitado");
        }

        //Si la excepción no es NOT_FOUND entonces ya es un error en la comunicación con el servicio producto -> Lo indicamos
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }


    //Método protegido con Circuit-Breaker
    @CircuitBreaker(name = "producto-service-write", fallbackMethod = "fallbackDescontarProductoStock")
    @Retry(name = "producto-service-write")
    public void descontarProductoStock(Long productoId, Integer cantidadDescontar){
            productoClient.descontarProductoStock(productoId, cantidadDescontar);
    }

    //fallback para -> descontarProductoStock
    public void fallbackDescontarProductoStock(Long productoId, Integer cantidadDescontar, Throwable ex){

        //Agregamos le warn al log indicando que el fallback fue activado por problema de comunicación
        log.warn("fallback activado para producto-service -> productoId={}", productoId, ex);

        //Si la excepción es NOT_FOUND, ya se sabe que es porque no se encontró el producto solicitado, entonces lo indicamos
        if(ex instanceof FeignException.NotFound){
            throw new ProductoNotFoundException("No se encontró producto con id: " + productoId);
        }

        //Si la excepción no es NOT_FOUND entonces ya es un error en la comunicación con el servicio producto -> Lo indicamos
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }

    //Protección de método para reponer stock a un producto en el servicio producto por su id
    @CircuitBreaker(name = "producto-service-write", fallbackMethod = "fallbackReponerStock")
    @Retry(name = "producto-service-write")
    public void reponerProductoStock(Long productoId, Integer cantidadReponer ){
        productoClient.reponerProductoStock(productoId, cantidadReponer);
    }

    //fallback para --> reponerProductoStock
    public void fallbackReponerStock(Long productoId, Integer cantidadReponer, Throwable ex ){

        //Agregamos le warn al log indicando que el fallback fue activado por problema de comunicación
        log.warn("fallback activado para producto-service -> productoId={}", productoId, ex);

        /*Si la excepción es NOT_FOUND, ya se sabe que es porque no se encontró el producto solicitado, entonces lo
         indicamos por medio de excepción*/
        if(ex instanceof FeignException.NotFound){
            throw new ProductoNotFoundException("No se encontró producto con id: " + productoId);
        }

        /*Si la excepción no es NOT_FOUND entonces ya es un error de infraestructura en la comunicación con el servicio
         producto -> Lo indicamos*/
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }

    //Método protegido por CB para encontrar un producto en producto-service por su id
    @CircuitBreaker(name = "producto-service-read", fallbackMethod = "fallbackFindProducto")
    @Retry(name = "producto-service-read")
    public ProductoIntegrationDto findProducto(Long productoId){
        return productoClient.findProducto(productoId);
    }

    //fallback para --> findProducto
    public ProductoIntegrationDto fallbackFindProducto(Long productoId, Throwable ex){

        //Agregamos le warn al log indicando que el fallback fue activado por problema de comunicación
        log.warn("fallback activado para producto-service -> productoId={}", productoId, ex);

        /*Si la excepción es NOT_FOUND, ya se sabe que es porque no se encontró el producto solicitado, entonces lo
         indicamos por medio de excepción*/
        if(ex instanceof FeignException.NotFound){
            throw new ProductoNotFoundException("No se encontró producto con id: " + productoId);
        }

        /*Si la excepción no es NOT_FOUND entonces ya es un error de infraestructura en la comunicación con el servicio
         producto -> Lo indicamos*/
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }
}
