package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.ProductoRequestDto;
import com.electrodostore.venta_service.dto.VentaCreadaDto;
import com.electrodostore.venta_service.dto.VentaResponseDto;

import java.util.List;

public interface IVentaService {

    /**
     * Consultas administrativa de ventas.
     */
    List<VentaResponseDto> findAllVentas();
    VentaResponseDto findVentaResponse(Long id);

    /**
     * Registra ventas del cliente autenticado.
     *
     * El cliente se obtendrá del Security Context para evitar que
     * clientes registren ventas en nombre de otros clientes
     * sin autorización
     */
    VentaCreadaDto saveVenta(List<ProductoRequestDto> productsList);

    /**
     * Cancela una venta del cliente autenticado.
     */
    void cancelVenta(Long id);

    /**
     * Cancela una venta mediante una operación administrativa.
     */
    void cancelVentaByAdmin(Long id);

    /**
     * Consulta las ventas asociadas a un cliente.
     */
    List<VentaResponseDto> findClienteVentas(Long clientId);
}
