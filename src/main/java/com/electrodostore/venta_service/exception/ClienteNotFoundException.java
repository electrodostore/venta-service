package com.electrodostore.venta_service.exception;

//Excepción personalizada para cuando no se encuentre un cliente consultado en cliente-service
public class ClienteNotFoundException extends RuntimeException{

    //Enviamos el mensaje a la superclase para poder acceder por medio de getMessage()
    public ClienteNotFoundException(String message){
        super(message);
    }
}
