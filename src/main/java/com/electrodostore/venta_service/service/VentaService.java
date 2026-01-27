package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.*;
import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.integration.ClienteIntegrationService;
import com.electrodostore.venta_service.integration.ProductoIntegrationService;
import com.electrodostore.venta_service.model.ProductoSnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class VentaService implements IVentaService{

    //Inyección de dependencia para la integración con producto-service
    //Inyección de dependencia para la integración con cliente-service
    private final ClienteIntegrationService clienteIntegration;
    private final ProductoIntegrationService productoIntegration;
    //Inyección por constructor
    public VentaService(ProductoIntegrationService productoIntegration, ClienteIntegrationService clienteIntegration){
        this.productoIntegration = productoIntegration;
        this.clienteIntegration = clienteIntegration;
    }

    //Método propio para buscar los supuestos productos que pertenecen a la venta
    private List<ProductoIntegrationDto> findProductos(List<Long> productosIds){
        //Si no hay productos a buscar, no tiene sentido buscar nada -> Excepción
        if(productosIds.isEmpty()){throw new ProductoNotFoundException("No se solicitó la información de ningún producto");}

        /*Intentamos hacer la búsqueda de los productos pasando por la capa de integración con producto-service
        donde se manejarán todas las excepcione relacionadas con esta comunicación*/
        return productoIntegration.findProductos(new HashSet<>(productosIds));
    }

    //Método propio para buscar al cliente dueño de una determinada venta
    private ClienteIntegrationDto findCliente(Long id){

        /*Intentamos hacer la búsqueda del cliente pasando por la capa de integración con cliente-service
        donde se manejarán todas las excepcione relacionadas con esta comunicación*/
        return clienteIntegration.findCliente(id);
    }
    
    @Override
    public List<VentaResponseDto> findAllVentas() {
        return List.of();
    }

    @Override
    public VentaResponseDto findVenta(Long id) {
        return null;
    }

    @Override
    public VentaResponseDto saveVenta(VentaRequestDto objNuevo) {
        return null;
    }

    @Override
    public void deleteVenta(Long id) {

    }

    @Override
    public VentaResponseDto updateVenta(Long id, VentaRequestDto objUpdated) {
        return null;
    }

    @Override
    public VentaResponseDto patchVenta(Long id, VentaRequestDto objUpdated) {
        return null;
    }
}
