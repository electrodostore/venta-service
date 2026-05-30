package com.electrodostore.venta_service.exception;

/**
 * Excepción base para todos los errores de negocio del dominio.
 *
 * Permite identificar y tratar de forma centralizada
 * las excepciones de dominio.
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}
