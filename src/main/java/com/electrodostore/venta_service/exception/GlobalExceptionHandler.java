package com.electrodostore.venta_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/*Clase manejadora de excepciones personalizadas -> Cuando ocurre una excepción Spring revisa esta clase
a ver si existe un handler que la maneje, si no lanza un handler por defecto*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Método propio para construir el mensaje de error de cada excepción personalizada
    private Map<String, Object> buildErrorMessage(HttpStatus status, String message, String errorCode){
        Map<String, Object> response = new LinkedHashMap<>();

        //Establecemos pares: clave-valor para construir la response
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("errorCode", errorCode);
        response.put("mensaje", message);

        return response;
    }

    //Manejador de la excepción VentaNoFound
    @ExceptionHandler(VentaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerVentaNotFound(VentaNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    //Manejador de la excepción ProductoNotFound
    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerProductoNotFound(ProductoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    //Manejador de la excepción ClienteNotFound
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlerClienteNotFound(ClienteNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    //Manejador de la excepción ProductoStockInsuficiente
    @ExceptionHandler(ProductoStockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handlerProductoStockInsuficiente(ProductoStockInsuficienteException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode().name()));
    }

    //Handler de la excepción personalizada ServiceUnavailable
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handlerServiceUnavailable(ServiceUnavailableException ex){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorMessage(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), ex.getErrorCode().name()));
    }

}
