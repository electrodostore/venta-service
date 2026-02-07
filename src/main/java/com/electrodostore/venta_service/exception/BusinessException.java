package com.electrodostore.venta_service.exception;

/*SuperClass de negocio, de la cual heredarán todas mis excepciones de dominio para que así puedan ser identificadas por una sola
 característica*/
public abstract class BusinessException extends RuntimeException {

    //El mensaje que se envíe desde las clases hijas, será subido a la superclase RuntimeException para que lo exponga
    protected BusinessException(String message) {
        super(message);
    }
}
