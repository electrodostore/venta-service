package com.electrodostore.venta_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centraliza el manejo de excepciones de la aplicación
 * y los transforma en respuestas HTTP personalizadas
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Construye el cuerpo de respuesta para errores controlados
    private Map<String, Object> buildErrorMessage(HttpStatus status, String message, String errorCode){
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("errorCode", errorCode);
        response.put("mensaje", message);

        return response;
    }

    @ExceptionHandler(VentaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerVentaNotFound(VentaNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerProductoNotFound(ProductoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerClienteNotFound(ClienteNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    @ExceptionHandler(ProductoStockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handlerProductoStockInsuficiente(ProductoStockInsuficienteException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorMessage(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrorCode().name()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handlerServiceUnavailable(ServiceUnavailableException ex){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorMessage(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), ex.getErrorCode().name()));
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<Map<String, Object>> handlerUnauthorizedOperation(UnauthorizedOperationException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorMessage(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getErrorCode().name()));
    }

}
