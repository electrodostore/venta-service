package com.electrodostore.venta_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

/**
 * Snapshot del producto al momento de realizar la venta.
 *
 * Permite conservar información histórica del producto
 * aunque este sea modificado posteriormente en su servicio origen.
 */
@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class ProductoSnapshot {

    /* Identificador del producto original.
       Forma parte de equals() y hashCode() para evitar
       productos duplicados en la venta. */
    @EqualsAndHashCode.Include
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer purchasedQuantity;
    private BigDecimal subTotal;
    private String productDescription;

}
