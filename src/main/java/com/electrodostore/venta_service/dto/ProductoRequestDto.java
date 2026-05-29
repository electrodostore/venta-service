package com.electrodostore.venta_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/*Clase Dto que me va a definir lo que el cliente me debe mandar desde la vista para consultar y guardar al
respectivo producto*/
public record ProductoRequestDto(
        @NotNull Long id,
        @NotNull @PositiveOrZero Integer quantity
) {}
