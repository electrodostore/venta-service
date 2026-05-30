package com.electrodostore.venta_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter  @Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Venta {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Integer totalItems;

    /* BigDecimal se utiliza para evitar problemas de precisión
       y redondeo propios de los tipos de punto flotante. */
    @Column(precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /* Colección de productos adquiridos en la venta.
       Cada elemento se almacena en una tabla secundaria. */
    @ElementCollection
    @CollectionTable(
            name = "products_of_sale",
            joinColumns = @JoinColumn(name = "venta_id")
    )
    private Set<ProductoSnapshot> listProducts = new HashSet<>();

    /* Snapshot del cliente al momento de la venta.
       Se almacena embebido para preservar los datos históricos
       aunque el cliente cambie posteriormente en su servicio origen. */
    @Embedded
    private ClienteSnapshot client;
    //Estado actual de la venta
    @Column(nullable = false)
    private VentaStatus status;

}
