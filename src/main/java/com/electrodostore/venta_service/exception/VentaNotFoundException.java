package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter  //-> Exposición de campo(s)

//Clase representativa de la excepción para cuando no se encuentre una venta
public class VentaNotFoundException extends BusinessException{

    //ErroCode identificativo de la excepción para servicios externos a este dominio
    private final VentaErrorCode errorCode;

    /*Mandamos el mensaje que venga como parámetro en la excepción a la superclase para que quede a
    disposición por medio del método getMessage()*/
    public VentaNotFoundException(String message){
        super(message);

        //Se le asigna como valor al errorCode el code correspondiente en el enum VentaErrorCode
        this.errorCode = VentaErrorCode.VENTA_NOT_FOUND;

    }
}
