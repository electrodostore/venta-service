package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter  //-> Exposición de campo(s)
//Excepción personalizada para cuando ocurra algún tipo de error en la comunicación con producto-service aparte de NOT_FOUND
public class ServiceUnavailableException extends RuntimeException{

    //ErroCode identificativo de esta excepción para otros servicios fuera de este dominio
    private final VentaErrorCode errorCode;

    //Ponemos a disponibilidad el mensaje que se manda en la excepción -> getMessage()
    public ServiceUnavailableException(String message){
        super(message);

        //Se define valor al campo errorCode con el code correspondiente en el enum VentaErrorCode
        this.errorCode = VentaErrorCode.PRODUCT_NOT_FOUND;
    }
}
