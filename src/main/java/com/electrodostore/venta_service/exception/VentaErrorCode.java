package com.electrodostore.venta_service.exception;

//Enum VentaErrorCode para guardar y asegurar cada errorCode que me identifica una excepción en particular fuera de este dominio
public enum VentaErrorCode {

    /*Definimos valores del enum que en realidad son varias instancias de este, cada una con el código identificativo de
     una excepción en particular*/
    PRODUCT_NOT_FOUND,
    PRODUCT_STOCK_INSUFICIENTE,
    CLIENT_NOT_FOUND,
    VENTA_NOT_FOUND,
    SERVICE_UNAVAILABLE

}
