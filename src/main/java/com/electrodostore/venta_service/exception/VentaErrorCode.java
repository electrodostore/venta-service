package com.electrodostore.venta_service.exception;

/**
 * Guarda códigos de error para que se identifique
 * a las excepciones fuera de este dominio
 */
public enum VentaErrorCode {

    PRODUCT_NOT_FOUND,
    PRODUCT_STOCK_INSUFICIENTE,
    CLIENT_NOT_FOUND,
    VENTA_NOT_FOUND,
    SERVICE_UNAVAILABLE,
    UNAUTHORIZED_OPERATION

}
