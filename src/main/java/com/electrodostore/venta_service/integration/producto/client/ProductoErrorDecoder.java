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

//Clase errorDecoder para descifrar el errorCode que viene dentro del body de una response desde producto-service
//Solo se va a descifrar este errorCode cuando el statusCode de la Response sea diferente de 2xx
/*En caso de que el statusCode sea 404 (recurso no encontrado) será necesario interpretar el errorCode para lanzar la respectiva
excepción de dominio indicando el problema a profundidad*/
@Slf4j
public class ProductoErrorDecoder implements ErrorDecoder {

    //Objeto encargado de deserializar el body de la Response de JSON a objeto java (errorResponse)
    private final ObjectMapper objectMapper = new ObjectMapper();

    /*Método implementado de la interfaz ErrorDecoder donde se definirá cuál será la excepción final que se retornará a Feign
      dependiendo de los diferentes valores que podrá tomar el statusCode y el errorCode de la response*/
    //methodKey --> Nombre del método feign que falló
    //response --> Respuesta HTTP completa con todos sus parámetros
    @Override
    public Exception decode(String methodKey, Response response) {

        try{
            /*Feign da el body de la Response como bytes (que en texto se traducen como el JSON enviado) los cuales se
             almacenan dentro de la clase InputStream*/
            InputStream bodyIs = response.body().asInputStream();

            //Si el body es null por algún motivo, no tiene sentido continuar
            if(bodyIs == null){return FeignException.errorStatus(methodKey, response);}

            //ObjMapper recibe esos bytes y construye el JSON para deserializarlo a un objeto de la clase ErrorResponseDto
            //Una vez se deserialize el JSON podremos acceder al errorCode y mensaje de la Response
            ErrorResponseDto error =
                    objectMapper.readValue(bodyIs, ErrorResponseDto.class);

            /*Si el statusCode de la Response es 404, quiere decir que hubo un recurso que no se encontró por algún
             motivo, entonces dentro del condicional se va a determinar el motivo preciso de la excepción
             por medio del errorCode que la Response haya traído*/
            if (response.status() == 404) {

                //Switch para definir la excepción de dominio que se va a retornar a Feign
                //Esta excepción dirá exactamente cuál es el problema en la petición hecha a producto-service
                switch (VentaErrorCode.valueOf(error.getErrorCode())) {

                    //Si el errorCode es "PRODUCT_NOT_FOUND" sabemos que la causa del error es que el producto no existe
                    case PRODUCT_NOT_FOUND:
                       return new ProductoNotFoundException(
                              error.getMensaje()
                       );

                    //Si el errorCode el "PRODUCT_STOCK_INSUFICIENTE" lo indicamos por medio de la excepción de este dominio
                    case PRODUCT_STOCK_INSUFICIENTE:
                        return new ProductoStockInsuficienteException(
                                error.getMensaje()
                        );

                    /*En caso de que el errorCode no coincida con ninguno de los anteriores, se construye la feignException
                     a partir de la Response original*/
                    default:
                        return FeignException.errorStatus(methodKey, response);
                }
            }

            //Si el statusCode es diferente de 404 -> FeignException
            return FeignException.errorStatus(methodKey, response);

        } catch (IOException e) {
            //Se agrega el log indicando el problema
            log.error("Error leyendo el body de la response de producto-service", e);
            //En caso de que haya algún error en el proceso de lectura del body -> FeignException
            return FeignException.errorStatus(methodKey, response);
        }
    }
}
