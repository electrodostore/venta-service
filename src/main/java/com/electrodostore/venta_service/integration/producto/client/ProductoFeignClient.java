package com.electrodostore.venta_service.integration.producto.client;

import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationDto;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cliente Feign para comunicarse con producto-service
 */
@FeignClient(name = "producto-service",
    configuration = ProductoFeignConfig.class)
public interface ProductoFeignClient {

    @GetMapping("/productos/{productoId}")
    ProductoIntegrationDto findProducto(@PathVariable Long productoId);

    @PostMapping("/productos/search")
    List<ProductoIntegrationDto> findProductos(@RequestBody List<Long> productsIds) ;

    @PostMapping("/productos/stock/verificar")
    void validarStock(@RequestBody List<ProductoIntegrationStockDto> productosValidarStock);

    @PatchMapping("/productos/stock/descontar")
    void descontarProductoStock(@RequestBody List<ProductoIntegrationStockDto> productosDescontarStock);

    @PatchMapping("/productos/stock/reponer")
    void reponerProductoStock(@RequestBody List<ProductoIntegrationStockDto> productosReponerStock);

}