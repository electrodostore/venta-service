package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.VentaRequestDto;
import com.electrodostore.venta_service.dto.VentaResponseDto;

import java.util.List;

public interface IVentaService {

    //Traer todas las ventas
    List<VentaResponseDto> findAllVentas();

    //Traer venta por Id
    VentaResponseDto findVentaResponse(Long id);

    //Registrar venta
    VentaResponseDto saveVenta(VentaRequestDto objNuevo);

    //Eliminar venta por Id
    void deleteVenta(Long id);

    //Actualizar completamente una venta
    VentaResponseDto updateVenta(Long id, VentaRequestDto objUpdated);

    //Actualizar parcialmente una venta
    VentaResponseDto patchVenta(Long id, VentaRequestDto objUpdated);

    //Método para encontrar la lista de Ventas de un determinado cliente por su id
    List<VentaResponseDto> findClienteVentas(Long clientId);
}
