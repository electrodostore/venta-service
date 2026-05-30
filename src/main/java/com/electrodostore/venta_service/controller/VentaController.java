package com.electrodostore.venta_service.controller;

import com.electrodostore.venta_service.dto.ProductoRequestDto;
import com.electrodostore.venta_service.dto.VentaCreadaDto;
import com.electrodostore.venta_service.dto.VentaResponseDto;
import com.electrodostore.venta_service.service.IVentaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private final IVentaService ventaService;

    public VentaController(IVentaService ventaService){
        this.ventaService = ventaService;
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> findAllVentas(){
        return ResponseEntity.ok(ventaService.findAllVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDto> findVenta(@PathVariable Long id){
        return ResponseEntity.ok(ventaService.findVentaResponse(id));
    }

    @GetMapping("/cliente/{clientId}")
    public ResponseEntity<List<VentaResponseDto>> findClienteVentas(@PathVariable Long clientId){
        return ResponseEntity.ok(ventaService.findClienteVentas(clientId));
    }

    @PostMapping
    public ResponseEntity<VentaCreadaDto> createVenta(@RequestBody @NotEmpty List<@NotNull @Valid ProductoRequestDto> productsList){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.saveVenta(productsList));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelVenta(@PathVariable Long id){
        ventaService.cancelVenta(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/admin/cancel")
    public ResponseEntity<Void> cancelVentaByAdmin(@PathVariable Long id){
        ventaService.cancelVentaByAdmin(id);

        return ResponseEntity.noContent().build();
    }

}
