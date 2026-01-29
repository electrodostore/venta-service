package com.electrodostore.venta_service.client;

import com.electrodostore.venta_service.dto.ProductoIntegrationDto;
import jakarta.ws.rs.Path;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*Interface donde se declararán y describirán los diferentes end-pints para acceder a los
métodos de producto-service*/
@FeignClient(name = "producto-service")//-> Mismo nombre con el que se registró en Eureka-server
public interface ProductoClient {

    //Descripción del método que traer una lista de productos a partir de sus ids
    @PostMapping("/productos/traer-productos-por-ids")
    List<ProductoIntegrationDto> findProductos(@RequestBody List<Long> productsIds) ;

    //Descripción del método que descuenta una cierta cantidad al stock de un determinado producto
    @PatchMapping("/productos/descontar-stock/{productoId}")
    void descontarProductoStock(@PathVariable Long productoId, @RequestBody Integer cantidadDescontar);

    //Descripción del método que se usa para reponer stock a un determinado producto ya sea porque se eliminó la venta o se actualizó
    @PatchMapping("/productos/reponer-stock/{productoId}")
    void reponerProductoStock(@PathVariable Long productoId, @RequestBody Integer cantidadReponer);

}