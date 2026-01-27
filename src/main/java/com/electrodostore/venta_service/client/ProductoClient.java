package com.electrodostore.venta_service.client;

import com.electrodostore.venta_service.dto.ProductoIntegrationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*Interface donde se declararán y describirán los diferentes end-pints para acceder a los
métodos de producto-service*/
@FeignClient(name = "producto-service")//-> Mismo nombre con el que se registró en Eureka-server
public interface ProductoClient {

    //Descripción del método que traer una lista de productos a partir de sus ids
    @PostMapping("/productos/traer-productos-por-ids")
    List<ProductoIntegrationDto> findProductos(@RequestBody List<Long> productsIds) ;

}