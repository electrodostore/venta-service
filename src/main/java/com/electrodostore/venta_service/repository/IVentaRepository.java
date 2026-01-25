package com.electrodostore.venta_service.repository;

import com.electrodostore.venta_service.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Interfaz que hereda de la clase de Jpa que me permite usar los diferentes métodos para las operaciones HTTP
@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {
}
