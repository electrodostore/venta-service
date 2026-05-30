package com.electrodostore.venta_service.integration.producto;

import com.electrodostore.venta_service.exception.BusinessException;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationDto;
import com.electrodostore.venta_service.exception.ServiceUnavailableException;
import com.electrodostore.venta_service.integration.producto.client.ProductoFeignClient;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationStockDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ProductoIntegrationService {

    private final ProductoFeignClient productoClient;
    public ProductoIntegrationService(ProductoFeignClient productoClient){
        this.productoClient = productoClient;
    }

    /**
     * Circuit Breaker para operaciones de lectura sobre producto-service.
     */
    @CircuitBreaker(name = "producto-service-read", fallbackMethod = "fallbackFindProductos")
    @Retry(name = "producto-service-read")
    public List<ProductoIntegrationDto> findProductos(Set<Long> productsIds){
        return productoClient.findProductos(new ArrayList<>(productsIds));
    }

    public List<ProductoIntegrationDto> fallbackFindProductos(Set<Long> productsIds, Throwable ex){

        // Propaga excepciones de negocio sin modificaciones.
        if (ex instanceof BusinessException be) {
            throw be;
        }

        //Informa problema de infraestructura en la comunicación
        log.warn("fallback activado para producto-service", ex);
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }

    /**
     * Circuit Breaker para descontar stock de
     * productos en producto-service
     */
    @CircuitBreaker(name = "producto-service-write", fallbackMethod = "fallbackDescontarProductoStock")
    @Retry(name = "producto-service-write")
    public void descontarProductosStock(List<ProductoIntegrationStockDto> productosDescontarStock){
        productoClient.descontarProductoStock(productosDescontarStock);
    }

    public void fallbackDescontarProductoStock(List<ProductoIntegrationStockDto> productosDescontarStock, Throwable ex){
        // Propaga excepciones de negocio sin modificaciones.
        if (ex instanceof BusinessException be) {
            throw be;
        }

        //Informa del error en la comunicación
        log.warn("fallback activado para producto-service", ex);
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }

    /**
     * Reponer stock de productos en producto-service
     * */
    @CircuitBreaker(name = "producto-service-write", fallbackMethod = "fallbackReponerStock")
    @Retry(name = "producto-service-write")
    public void reponerProductosStock(List<ProductoIntegrationStockDto> productosReponerStock){
        productoClient.reponerProductoStock(productosReponerStock);
    }

    public void fallbackReponerStock(List<ProductoIntegrationStockDto> productosReponerStock, Throwable ex ){
        // Propaga excepciones de negocio sin modificaciones.
        if (ex instanceof BusinessException be) {
            throw be;
        }

        /*Informa error de infraestructura en la
         * comunicación con producto-service*/
        log.warn("fallback activado para producto-service.", ex);
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }

    /**
     * Valida stock de productos en producto-service
     * */
    @CircuitBreaker(name = "producto-service-write", fallbackMethod = "fallbackValidarProductosStock")
    @Retry(name = "producto-service-write")
    public void validarProductosStock(List<ProductoIntegrationStockDto> productosValidarStock){
        productoClient.validarStock(productosValidarStock);
    }

    public void fallbackValidarProductosStock(List<ProductoIntegrationStockDto> productosValidarStock, Throwable ex){
        //Propaga excepciones de dominio sin modificaciones
        if(ex instanceof BusinessException be){
            throw be;
        }

        //Informa el problema de infraestructura
        log.warn("fallback activado en validación de stock de productos", ex);
        throw new ServiceUnavailableException("No se pudo establecer la comunicación con producto-service. Intente de nuevo más tarde");
    }

    /**
     * Recupera un producto desde producto-service.
     */
    @CircuitBreaker(name = "producto-service-read", fallbackMethod = "fallbackFindProducto")
    @Retry(name = "producto-service-read")
    public ProductoIntegrationDto findProducto(Long productoId){
        return productoClient.findProducto(productoId);
    }

    public ProductoIntegrationDto fallbackFindProducto(Long productoId, Throwable ex){
        //Propaga excepciones de dominio sin modificación
        if (ex instanceof BusinessException be) {
            throw be;
        }

        //Informa error de infraestructura
        log.warn("fallback activado para producto-service -> productoId={}", productoId, ex);
        throw new ServiceUnavailableException("No se pudo establecer comunicación con producto-service. Por favor intente más tarde");
    }
}
