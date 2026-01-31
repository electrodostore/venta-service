package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter //-> Exposición de campo(s)

//Excepción personalizada para cuando no se encuentre un cliente consultado en cliente-service
public class ClienteNotFoundException extends RuntimeException{

    //ErroCode identificativo de esta excepción para otros servicios fuera de este dominio
    private final VentaErrorCode errorCode;

    //Enviamos el mensaje a la superclase para poder acceder por medio de getMessage()
    public ClienteNotFoundException(String message){
        super(message);
        //Se define valor al campo errorCode con el code correspondiente en el enum VentaErrorCode
        this.errorCode = VentaErrorCode.CLIENT_NOT_FOUND;
    }

}
