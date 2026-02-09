package com.electrodostore.venta_service.repository;

import com.electrodostore.venta_service.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//Interfaz que hereda de la clase de Jpa que me permite usar los diferentes métodos para las operaciones HTTP
@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

    //Método personalizado que SpringData Jpa interpreta para traerme la lista de las ventas que comparten cliente con la misma id
    List<Venta> findByClient_clientId(Long clientId);
}
