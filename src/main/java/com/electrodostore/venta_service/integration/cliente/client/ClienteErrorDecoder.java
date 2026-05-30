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

/**
 * Decodifica errorCodes en respuestas de error provenientes de cliente-service
 * e intenta traducirlas a excepciones conocidas en este dominio
 */
@Slf4j
public class ClienteErrorDecoder implements ErrorDecoder {

    ObjectMapper objMapper = new ObjectMapper();

   /**
    * Convierte respuestas de error de Feign a excepciones de dominio
    * cuando se logran interpretar
    * */
    @Override
    public Exception decode(String methodKey, Response response) {
        try{
            InputStream bodyIs = response.body().asInputStream();

            if(bodyIs == null){return FeignException.errorStatus(methodKey, response);}

           //Lee el cuerpo de la respuesta y lo mapea a DTO de error
            ErrorResponseDto error = objMapper.readValue(bodyIs, ErrorResponseDto.class);

            //En este dominio solo se interpretan errores HTTP 404
            if(response.status() == 404) {

                //Compara la respuesta de error con excepciones conocidas
                switch (VentaErrorCode.valueOf(error.getErrorCode())) {

                    case CLIENT_NOT_FOUND:
                        return new ClienteNotFoundException(
                                error.getMensaje()
                        );

                    //Si no hay coincidencias, se conserva la excepción original Feign
                    default:
                        return FeignException.errorStatus(methodKey, response);
                }
            }

            return FeignException.errorStatus(methodKey, response);

        //Si hay un error leyendo el cuerpo de la respuesta se retorna excepción Feign predeterminada
        }catch(IOException e){
            log.error("Error leyendo el body de la response proveniente de cliente-service");
            return FeignException.errorStatus(methodKey, response);
        }

    }
}
