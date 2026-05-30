package com.electrodostore.venta_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Snapshot embebido que representa el estado del cliente
 * al momento de la venta.
 *
 * No posee identidad propia; únicamente conserva una referencia
 * (clientId) a la entidad Cliente del servicio cliente-service.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ClienteSnapshot {

    private Long clientId;
    private String clientName;
    private String clientCellphone;
    private String clientDocument;
    private String clientAddress;
}
