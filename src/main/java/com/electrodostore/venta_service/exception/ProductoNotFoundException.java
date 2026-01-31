package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter //-> Exposición de campo(s)

//Exception NotFound para cuando no se logre encontrar algún producto consultado en producto-service
public class ProductoNotFoundException extends RuntimeException{

    //ErroCode identificativo de esta excepción para otros servicios fuera de este dominio
    private final VentaErrorCode errorCode;

    //Enviamos el mensaje a la superclase para poder acceder por medio de getMessage()
    public ProductoNotFoundException(String message){
        super(message);

        //Se define valor al campo errorCode con el code correspondiente en el enum VentaErrorCode
        this.errorCode = VentaErrorCode.PRODUCT_NOT_FOUND;
    }

}
