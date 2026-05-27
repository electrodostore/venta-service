package com.electrodostore.venta_service.controller;

import com.electrodostore.venta_service.dto.ProductoRequestDto;
import com.electrodostore.venta_service.dto.VentaCreadaDto;
import com.electrodostore.venta_service.dto.VentaResponseDto;
import com.electrodostore.venta_service.service.IVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    //Inyección de dependencia por constructor para el service de Venta
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

    @GetMapping("/traer-ventas-de-cliente/{clientId}")
    public ResponseEntity<List<VentaResponseDto>> findClienteVentas(@PathVariable Long clientId){
        return ResponseEntity.ok(ventaService.findClienteVentas(clientId));
    }

    @PostMapping
    public ResponseEntity<VentaCreadaDto> saveVenta(@RequestBody List<ProductoRequestDto> productsList){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.saveVenta(productsList));
    }

    @DeleteMapping("/cancel-venta/{id}")
    public ResponseEntity<Void> cancelVenta(@PathVariable Long id){
        ventaService.cancelVenta(id);

        return ResponseEntity.noContent().build();
    }
}
