package com.electrodostore.venta_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/*Definimos clase @Embeddable para sacar objetos embebidos de esta con información del estado de todos los
productos que pertenece a la venta en el momento que se realiza la venta*/
@Embeddable
public class ProductoSnapshot {

    /*Referencia (productId) a la identidad  del producto original, y por la que se comparan los Snapshot
    embebidos para evitas duplicidad*/
    @EqualsAndHashCode.Include
    private Long productId;
    private String productName;
    //Cantidad comprada del producto
    private Integer productQuantity;
    //Subtotal = precio * productQuantity
    private Double subTotal;
    private String productDescription;

}
