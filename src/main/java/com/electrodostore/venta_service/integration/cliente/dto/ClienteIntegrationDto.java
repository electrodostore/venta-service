package com.electrodostore.venta_service.integration.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClienteIntegrationDto {

    private Long id;
    private String name;
    private String cellphone;
    private String document;
    private String address;

}
