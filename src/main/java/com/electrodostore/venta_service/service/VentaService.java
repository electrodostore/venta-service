package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.*;
import com.electrodostore.venta_service.exception.ClienteNotFoundException;
import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.exception.UnauthorizedOperationException;
import com.electrodostore.venta_service.exception.VentaNotFoundException;
import com.electrodostore.venta_service.integration.cliente.ClienteIntegrationService;
import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import com.electrodostore.venta_service.integration.producto.ProductoIntegrationService;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationDto;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationStockDto;
import com.electrodostore.venta_service.model.ClienteSnapshot;
import com.electrodostore.venta_service.model.ProductoSnapshot;
import com.electrodostore.venta_service.model.Venta;
import com.electrodostore.venta_service.model.VentaStatus;
import com.electrodostore.venta_service.repository.IVentaRepository;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class VentaService implements IVentaService {

    private final ClienteIntegrationService clienteIntegration;
    private final ProductoIntegrationService productoIntegration;
    private final IVentaRepository ventaRepo;

    public VentaService(ProductoIntegrationService productoIntegration, ClienteIntegrationService clienteIntegration, IVentaRepository ventaRepo) {
        this.productoIntegration = productoIntegration;
        this.clienteIntegration = clienteIntegration;
        this.ventaRepo = ventaRepo;
    }

    /**
     * Busca una lista de productos por sus ids
     * */
    private List<ProductoIntegrationDto> findProductos(List<Long> productosIds){

        if(productosIds.isEmpty()){throw new ProductoNotFoundException("No se solicitó la información de ningún producto");}

        if(productosIds.size() == 1){
            return List.of(
                    productoIntegration.findProducto(productosIds.get(0))
            );
        }

        return productoIntegration.findProductos(new HashSet<>(productosIds));
    }

    /**
     * Busca el cliente propietario de una venta.
     */
    private ClienteIntegrationDto findCliente(Long id){
        return clienteIntegration.findCliente(id);
    }

    /**
     * Convierte un cliente obtenido desde cliente-service
     * a un snapshot persistible.
     */
    private ClienteSnapshot clienteIntegrationToSnapshot(ClienteIntegrationDto clienteIntegrado){
        return new ClienteSnapshot(clienteIntegrado.getId(), clienteIntegrado.getName(), clienteIntegrado.getCellphone(),
                clienteIntegrado.getDocument(), clienteIntegrado.getAddress());
    }

    /**
     * Extrae los ids de los productos solicitados.
     */
    private List<Long> sacarProductosIds(List<ProductoRequestDto> listProductos){

        List<Long> productosIds = new ArrayList<>();
        for(ProductoRequestDto productoRequest: listProductos){
            productosIds.add(
                    productoRequest.id()
            );
        }

        return productosIds;
    }

    /**
     * Verifica que todos los productos solicitados
     * fueron encontrados.
     */
    private void verificarCargaCompletaDeProductos(List<ProductoIntegrationDto> productosIntegrados, List<Long> productosIds){

        // Se convierte a Set para eliminar ids duplicados antes de validar.
        if(productosIntegrados.size() < (new HashSet<>(productosIds)).size()){throw new ProductoNotFoundException("Uno o más productos no fueron encontrados");}
    }

    /**
     * Construye los DTO utilizados para operaciones
     * de stock en producto-service.
     */
    private List<ProductoIntegrationStockDto> productosRequestToIntegration(List<ProductoRequestDto> productosRequest){

        List<ProductoIntegrationStockDto> productosIntegration = new ArrayList<>();

        for(ProductoRequestDto productoValidar: productosRequest){
            productosIntegration.add(
                    new ProductoIntegrationStockDto(
                            productoValidar.id(),
                            productoValidar.quantity()
                    )
            );
        }

        return productosIntegration;
    }

    /**
     * Construye los DTO necesarios para reponer stock
     * a partir de snapshots persistidos.
     */
    private List<ProductoIntegrationStockDto> productosSnapshotToIntegration(Set<ProductoSnapshot> productosSnapshot){

        List<ProductoIntegrationStockDto> productosIntegration = new ArrayList<>();

        for(ProductoSnapshot productoSnapshot: productosSnapshot){
            productosIntegration.add(
                    new ProductoIntegrationStockDto(
                            productoSnapshot.getProductId(),
                            productoSnapshot.getPurchasedQuantity()
                    )
            );
        }

        return productosIntegration;
    }


    /**
     * Valida que exista stock suficiente para los
     * productos solicitados.
     */
    private void verificarProductosStock(List<ProductoRequestDto> productosValidarStock){

        List<ProductoIntegrationStockDto> productosIntegrar = productosRequestToIntegration(productosValidarStock);

        productoIntegration.validarProductosStock(
                productosIntegrar
        );
    }

    /**
     * Descuenta stock de los productos comprados.
     */
    private void descontarProductosStock(List<ProductoRequestDto> productosRequest){
        List<ProductoIntegrationStockDto> productosIntegration = productosRequestToIntegration(productosRequest);

        productoIntegration.descontarProductosStock(
                productosIntegration
        );
    }

    /**
     * Repone el stock de los productos indicados.
     */
    private void reponerProductosStock(Set<ProductoSnapshot> listProductos) {
        List<ProductoIntegrationStockDto> productosIntegration = productosSnapshotToIntegration(listProductos);

        productoIntegration.reponerProductosStock(
                productosIntegration
        );
    }

    /**
     * Convierte productos integrados a snapshots
     * listos para persistencia.
     */
    private List<ProductoSnapshot> productosIntegrationToSnapshot(List<ProductoRequestDto> productosRequest, List<ProductoIntegrationDto> productosIntegration){
        // Valida disponibilidad de stock antes de construir los snapshots.
        verificarProductosStock(productosRequest);

        List<ProductoSnapshot> productosSnapshot = new ArrayList<>();

        /* Relaciona cada producto obtenido desde producto-service
         * con su correspondiente solicitud para construir el snapshot.
         */
        for(ProductoIntegrationDto objIntegration: productosIntegration){

            for(ProductoRequestDto objRequest: productosRequest){

                if(objIntegration.getId().equals(objRequest.id())){

                    // Calcula el subtotal según la cantidad comprada.
                    BigDecimal subTotal = objIntegration.getPrice().multiply(
                            BigDecimal.valueOf(objRequest.quantity())
                    );

                    //Construye Snapshot con los datos correspondientes
                    productosSnapshot.add(
                            new ProductoSnapshot(
                                    objIntegration.getId(),
                                    objIntegration.getName(),
                                    objIntegration.getPrice(),
                                    objRequest.quantity(),
                                    subTotal,
                                    objIntegration.getDescription()
                            )
                    );

                    // Una vez construido el snapshot, continúa con el siguiente producto.
                    break;
                }
            }
        }

        /* Una vez generados correctamente los snapshots,
         * se descuenta el stock correspondiente.
         */
        descontarProductosStock(productosRequest);

        return productosSnapshot;

    }

    /**
     * Convierte snapshots de productos a DTO de respuesta.
     */
    private List<ProductoResponseDto> productosSnapshotToResponse(List<ProductoSnapshot> productosSnapshot){

        List<ProductoResponseDto> productosResponse = new ArrayList<>();

        for(ProductoSnapshot objSnapshot: productosSnapshot){

            productosResponse.add(
                    new ProductoResponseDto(
                            objSnapshot.getProductId(),
                            objSnapshot.getProductName(),
                            objSnapshot.getProductPrice(),
                            objSnapshot.getPurchasedQuantity(),
                            objSnapshot.getSubTotal(),
                            objSnapshot.getProductDescription()
                    )
            );
        }

        return productosResponse;
    }

    /**
     * Convierte un cliente snapshot a DTO de respuesta.
     */
    private ClienteResponseDto clienteSnapshotToResponse(ClienteSnapshot objCliente){

        return new ClienteResponseDto(
                objCliente.getClientId(),
                objCliente.getClientName(),
                objCliente.getClientCellphone(),
                objCliente.getClientDocument(),
                objCliente.getClientAddress()
        );
    }

    /**
     * Construye la respuesta de una venta.
     */
    private VentaResponseDto buildVentaResponse(Venta objVenta){

        return new VentaResponseDto(
                objVenta.getId(),
                objVenta.getDate(),
                objVenta.getTotalItems(),
                objVenta.getTotalPrice(),
                objVenta.getStatus(),
                //Método propio para preparar productos
                productosSnapshotToResponse(new ArrayList<>(objVenta.getListProducts())),
                //Método propio para preparar cliente
                clienteSnapshotToResponse(objVenta.getClient())
        );
    }

    /**
     * Calcula el valor total de la venta.
     */
    private BigDecimal calcularTotalPrice(List<ProductoSnapshot> productosComprados){
        BigDecimal totalPrice = BigDecimal.ZERO;

        for(ProductoSnapshot objSnapshot: productosComprados){
            totalPrice = totalPrice.add(objSnapshot.getSubTotal());
        }

        return  totalPrice;
    }

    /**
     * Calcula la cantidad total de unidades compradas.
     */
    private Integer calcularTotalItems(List<ProductoSnapshot> productosComprados){
        Integer totalItems = 0;

        for(ProductoSnapshot objSnapshot: productosComprados){
            totalItems += objSnapshot.getPurchasedQuantity();
        }

        return totalItems;
    }


    /**
     * Construye una venta lista para persistencia.
     */
    private Venta buildVentaPersistir(List<ProductoRequestDto> productsList){

        // Obtiene el cliente autenticado que realiza la compra.
        ClienteIntegrationDto cliente = findCliente(
                getAuthenticatedClientId()
        );

        ClienteSnapshot clienteSnapshot = clienteIntegrationToSnapshot(cliente);

        List<Long> productosIds = sacarProductosIds(productsList);

        // Recupera la información de los productos solicitados.
        List<ProductoIntegrationDto> productosIntegration = findProductos(productosIds);

        verificarCargaCompletaDeProductos(productosIntegration, productosIds);

        // Convierte los productos a snapshots persistibles.
        List<ProductoSnapshot> productosSnapshot = productosIntegrationToSnapshot(productsList, productosIntegration);

        return(
                new Venta(
                    null, // El identificador será generado por la base de datos.
                    LocalDate.now(),
                    calcularTotalItems(productosSnapshot),
                    calcularTotalPrice(productosSnapshot),
                    new HashSet<>(productosSnapshot),
                    clienteSnapshot,
                    VentaStatus.PENDING  // Estado inicial antes de completar el proceso.
                )
        );
    }

    /**
     * Obtiene el id del cliente autenticado desde el JWT.
     */
    private Long getAuthenticatedClientId(){
        // Recupera la autenticación actual.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Extrae los claims almacenados en el JWT.
        Jwt principal = (Jwt) authentication.getPrincipal();

        // Obtiene el identificador de negocio del cliente.
        Number clientId = principal.getClaim("clientId");

        // Valida que el usuario autenticado sea un cliente.
        if(clientId == null){throw new UnauthorizedOperationException("El usuario no es cliente, por lo que " +
                "no puede realizar la operación");
        }

        return  clientId.longValue();

    }

    /**
     * Busca una venta para operaciones internas.
     */
    protected Venta findVenta(Long id){
        Optional<Venta> objVenta =  ventaRepo.findById(id);

        if(objVenta.isEmpty()){throw new VentaNotFoundException("No se encontró Venta con id: " + id);}

        return objVenta.get();
    }

    @Transactional(readOnly = true)
    @Override
    public List<VentaResponseDto> findAllVentas() {
        List<VentaResponseDto> listVentas = new ArrayList<>();

        for(Venta objVenta: ventaRepo.findAll()){
            listVentas.add(
                    buildVentaResponse(objVenta)
            );
        }

        return listVentas;
    }

    @Transactional(readOnly = true)
    @Override
    public VentaResponseDto findVentaResponse(Long id) {
        return buildVentaResponse(
                findVenta(id)
        );
    }

    @Transactional
    @Override
    public VentaCreadaDto saveVenta(List<ProductoRequestDto> productsList) {

        Venta objVenta = buildVentaPersistir(productsList);

        //Marca la venta como completada
        objVenta.setStatus(VentaStatus.COMPLETED);

        ventaRepo.save(objVenta);

        return new VentaCreadaDto(objVenta.getId());
    }

    @Transactional
    @Override
    public void cancelVenta(Long id) {
        Venta objVenta = findVenta(id);

        //Valida que la venta realmente le pertenezca al cliente autenticado
        if(!objVenta.getClient().getClientId().equals(getAuthenticatedClientId())){
            throw new UnauthorizedOperationException("Cliente no autorizado para realizar la operación");
        }

        objVenta.setStatus(VentaStatus.CANCELED);

        //Reponemos al stock de cada producto la cantidad que fue comprada
        reponerProductosStock(objVenta.getListProducts());
    }

    @Transactional
    @Override
    public void cancelVentaByAdmin(Long id) {
        Venta objVenta = findVenta(id);

        objVenta.setStatus(VentaStatus.CANCELED);

        //Reponemos al stock de cada producto la cantidad que fue comprada
        reponerProductosStock(objVenta.getListProducts());
    }

    @Transactional(readOnly = true)
    @Override
    public List<VentaResponseDto> findClienteVentas(Long clientId) {

        List<VentaResponseDto> listVentas = new ArrayList<>();

        //Recorre las ventas del cliente
        for(Venta objVenta: ventaRepo.findByClient_clientId(clientId)){
            //Construye  DTOs y los agrega a la lista
            listVentas.add(buildVentaResponse(objVenta));
        }

        return listVentas;
    }
}
