package com.electrodostore.venta_service.exception;

//Clase representativa de la excepción para cuando no se encuentre una venta
public class VentaNotFoundException extends RuntimeException{

    /*Mandamos el mensaje que venga como parametro en la excepción a la superclase para que quede a
    disposición por medio del método getMessage()*/
    public VentaNotFoundException(String message){
        super(message);
    }
}
