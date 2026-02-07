package com.electrodostore.venta_service.integration.cliente.client;

import com.electrodostore.venta_service.exception.ClienteNotFoundException;
import com.electrodostore.venta_service.exception.VentaErrorCode;
import com.electrodostore.venta_service.integration.common.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/*Clase que se usará para transformar excepciones provenientes del servicio cliente a excepciones de este dominio por medio de
la decodificación de errorCodes que identifican a cada excepción de cliente-service*/
@Slf4j
public class ClienteErrorDecoder implements ErrorDecoder {

    /*Objeto encargado de deserealizar un body de una Response en formato JSON a un objeto de la clase de transferencia de
     datos: ErrorResponseDto*/
    ObjectMapper objMapper = new ObjectMapper();

    /*Método Implementado de la interfaz de Feign: ErrorDecoder, el cuál le retornará a Feign la respectiva excepción
     ya sea de dominio o FeignException dependiendo de los valores que pueda haber en el errorCode y statusCode de la response*/
    //methodKey -> Método Feign que hizo la petición y falló
    //response -> response completa con todos sus parámetros
    @Override
    public Exception decode(String methodKey, Response response) {
        try{
            /*Buscamos el body de la Response que en este momento está formado por Bytes (Pero en formato texto sigue
             siendo el mismo JSON) y lo agregamos a un objeto InputStream*/
            InputStream bodyIs = response.body().asInputStream();

            //Si el body es null por algún motivo, no tiene sentido continuar
            if(bodyIs == null){return FeignException.errorStatus(methodKey, response);}

            /*objMapper usa los bytes en el objeto InputStream para construir el JSON y posteriormente
             deserealizarlo a un objeto de mi clase DTO errorResponseDto*/
            //Finalmente, ya tenemos dentro del objeto "error" el errorCode y mensaje de la Response que provoca el error
            ErrorResponseDto error = objMapper.readValue(bodyIs, ErrorResponseDto.class);

            //Solo se compara el errorCode de la Response si el fallo es por 404 -> recurso no encontrado
            if(response.status() == 404) {

                //Se saca un objeto de VentaErrorCode y se le asigna el valor del errorCode de la Response para compararlo
                switch (VentaErrorCode.valueOf(error.getErrorCode())) {

                    //En caso de el errorCode sea CLIENT_NOT_FOUND -> Excepción de dominio ClienteNotFound
                    case CLIENT_NOT_FOUND:
                        return new ClienteNotFoundException(
                                error.getMensaje()
                        );

                    //Si no hay coincidencias, retornamos FeignException a partir de la Response obtenida
                    default:
                        return FeignException.errorStatus(methodKey, response);
                }
            }

            //Si el statusCode de la response es diferente de 404 -> FeignException a partir de la response
            return FeignException.errorStatus(methodKey, response);

        //Si hay algún error en la entrada o salida de datos -> FeignException basado en la Response
        }catch(IOException e){
            //Se agrega un log indicando el fallo
            log.error("Error leyendo el body de la response proveniente de cliente-service");
            return FeignException.errorStatus(methodKey, response);
        }

    }
}
