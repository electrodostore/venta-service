package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.ProductoRequestDto;
import com.electrodostore.venta_service.dto.VentaCreadaDto;
import com.electrodostore.venta_service.dto.VentaResponseDto;

import java.util.List;

public interface IVentaService {

    //Traer todas las ventas
    List<VentaResponseDto> findAllVentas();

    //Traer venta por Id
    VentaResponseDto findVentaResponse(Long id);

    /**
     * Registra ventas del cliente autenticado.
     *
     * El cliente se obtendrá del Security Context para evitar que
     * clientes registren ventas a nombre de otros clientes.
     */
    VentaCreadaDto saveVenta(List<ProductoRequestDto> productsList);

    //Borrado lógico de venta para evitar perdida de registros históricos
    void cancelVenta(Long id);

    //Borrado administrativo y lógico de venta
    void cancelVentaByAdmin(Long id);

    //Método para encontrar la lista de Ventas de un determinado cliente por su id
    List<VentaResponseDto> findClienteVentas(Long clientId);
}
