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

    //totalItems = Cantidad total de unidades de productos compradas en la venta
    private Integer totalItems;

    /*BigDecimal es más recomendado que Double para cuestiones de precio
    ya que Double tiene problemas de redondeo y exactitud mientras que BigDecimal es más preciso*/
    /*Precision define la cántidad máxima de digitos que puede tener el total en la base de datos y scale la
    cantidad de decimales*/
    @Column(precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /* @ElementCollection define una colección de objetos embebidos
   (ProductoSnapshot) que se almacenan en una tabla secundaria.
   Cada registro representa un producto incluido en la venta */
    @ElementCollection
    @CollectionTable(
            /*En el caso de las colecciones, los objetos embebidos se almacenan en una table secundaria con
            el nombre de "products_of_sale*/
            name = "products_of_sale",
            //Le definimos el nombre a la FK en products_of_sale que hará referencia a la PK de la venta correspondiente
            joinColumns = @JoinColumn(name = "venta_id")
    )
    private Set<ProductoSnapshot> listProducts = new HashSet<>();

    /* @Embedded indica que ClienteSnapshot es un objeto embebido cuyo estado
   se persiste como parte de la entidad Venta. Este snapshot representa
   una copia del estado del Cliente en el momento de la venta y contiene
   únicamente una referencia (clientId) a la identidad del Cliente original,
   la cual pertenece a un servicio externo */
    @Embedded
    private ClienteSnapshot client;

}
