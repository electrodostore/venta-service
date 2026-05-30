package com.electrodostore.venta_service.dto;

/**
 * DTO encargado de transferir el dato mínimo necesario
 * cuando se crea una venta --> El ID de esta.
 *
 *
 * Si después se necesita más detalle, se podrá consultar por este mismo ID
 */
public record VentaCreadaDto(
        Long ventaId
) {}
