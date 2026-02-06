package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter //Exponemos campo(s)
//Excepción personalizada para cuando un producto no tenga suficiente stock para una determinada operación
public class ProductoStockInsuficienteException extends RuntimeException{

    //Declaramos la variable que va a guardar el respectivo errorCode de la excepción
    private final VentaErrorCode errorCode;

    //Definimos que al llamar a esta excepción se envíe un parámetro con el mensaje de error que la causó
    public ProductoStockInsuficienteException(String message){
        //Dicho mensaje lo enviamos a la superclase RuntimeException para tenerlo disponible en el método: getMessage()
        super(message);

        //Se le define valor al errorCode
        errorCode = VentaErrorCode.PRODUCT_STOCK_INSUFICIENTE;
    }
}
