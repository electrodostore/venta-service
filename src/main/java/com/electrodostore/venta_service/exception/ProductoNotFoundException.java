package com.electrodostore.venta_service.exception;

//Exeption NotFound para cuando no se logre encontrar algún producto consultado en producto-service
public class ProductoNotFoundException extends RuntimeException{

    //Enviamos el mensaje a la superclase para poder acceder por medio de getMessage()
    public ProductoNotFoundException(String message){
        super(message);
    }
}
