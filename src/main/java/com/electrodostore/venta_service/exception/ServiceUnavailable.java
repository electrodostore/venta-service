package com.electrodostore.venta_service.exception;

//Excepción personalizada para cuando ocurra algún tipo de error en la comunicación con producto-service aparte de NOT_FOUND
public class ServiceUnavailable extends RuntimeException{

    //Ponemos a disponibilidad el mensaje que se manda en la excepción -> getMessage()
    public ServiceUnavailable(String message){
        super(message);
    }
}
