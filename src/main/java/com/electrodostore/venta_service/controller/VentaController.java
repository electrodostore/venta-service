package com.electrodostore.venta_service.controller;

import com.electrodostore.venta_service.dto.VentaRequestDto;
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

    @PostMapping
    public ResponseEntity<VentaResponseDto> saveVenta(@RequestBody VentaRequestDto objNuevo){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.saveVenta(objNuevo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id){
        ventaService.deleteVenta(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDto> updateVenta(@PathVariable Long id, @RequestBody VentaRequestDto objUpdated){
        return ResponseEntity.ok(ventaService.updateVenta(id, objUpdated));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<VentaResponseDto> patchVenta(@PathVariable Long id, @RequestBody VentaRequestDto objUpdated){
        return ResponseEntity.ok(ventaService.patchVenta(id, objUpdated));
    }
}
