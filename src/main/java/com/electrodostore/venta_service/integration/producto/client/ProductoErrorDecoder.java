package com.electrodostore.venta_service.integration.producto.client;

import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.exception.ProductoStockInsuficienteException;
import com.electrodostore.venta_service.exception.VentaErrorCode;
import com.electrodostore.venta_service.integration.common.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Traduce respuestas de error de producto-service
 * a excepciones de dominio conocidas por venta-service.
 */
@Slf4j
public class ProductoErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte una respuesta de error de Feign
     * en una excepción de dominio cuando es posible.
     */
    @Override
    public Exception decode(String methodKey, Response response) {

        try{
            //Obtiene el cuerpo de la respuesta de error.
            InputStream bodyIs = response.body().asInputStream();

            if(bodyIs == null){return FeignException.errorStatus(methodKey, response);}

            // Leemos body y lo mapeamos a DTO de error
            ErrorResponseDto error =
                    objectMapper.readValue(bodyIs, ErrorResponseDto.class);

            // Procesa errores de negocio expuestos mediante HTTP 404.
            if (response.status() == 404) {

                //Intenta transformar la response de error en error conocido
                switch (VentaErrorCode.valueOf(error.getErrorCode())) {

                    case PRODUCT_NOT_FOUND:
                       return new ProductoNotFoundException(
                              error.getMensaje()
                       );

                    case PRODUCT_STOCK_INSUFICIENTE:
                        return new ProductoStockInsuficienteException(
                                error.getMensaje()
                        );

                    //Si el error no es conocido en este dominio, lanzamos excepción Feign
                    default:
                        return FeignException.errorStatus(methodKey, response);
                }
            }

            // Para códigos no manejados se conserva la excepción original de Feign.
            return FeignException.errorStatus(methodKey, response);

            //Retorna excepción Feign si hay problemas leyendo el body de la response
        } catch (IOException e) {
            log.error("Error leyendo el body de la response de producto-service", e);
            return FeignException.errorStatus(methodKey, response);
        }
    }
}
