package com.electrodostore.venta_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Estos métodos se usan para comparar dos objetos y evitar duplicidad de Snapshots embebidos
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/* Snapshot embebido que representa el estado del Cliente en el momento
   de la venta. No posee identidad propia; únicamente contiene una
   referencia (clientId) a la entidad Cliente del servicio cliente-service */
@Embeddable
public class ClienteSnapshot {

    //Igualdad basada en clientId para evitar Snapshots duplicadas
    @EqualsAndHashCode.Include
    private Long clientId;
    private String clientName;
    private String clientCellphone;
    private String clientDocument;
    private String clientAddress;
}
