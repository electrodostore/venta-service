package com.electrodostore.venta_service.repository;

import com.electrodostore.venta_service.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Venta.
 */
@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Recupera las ventas asociadas a un cliente
     */
    List<Venta> findByClient_clientId(Long clientId);
}
