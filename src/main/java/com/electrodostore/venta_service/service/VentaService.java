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
public class VentaService implements IVentaService{

    //Inyección de dependencia para la integración con producto-service
    //Inyección de dependencia para la integración con cliente-service
    //Inyección de dependencia para el repositorio del servicio Venta
    private final ClienteIntegrationService clienteIntegration;
    private final ProductoIntegrationService productoIntegration;
    private final IVentaRepository ventaRepo;
    //Inyección por constructor
    public VentaService(ProductoIntegrationService productoIntegration, ClienteIntegrationService clienteIntegration, IVentaRepository ventaRepo){
        this.productoIntegration = productoIntegration;
        this.clienteIntegration = clienteIntegration;
        this.ventaRepo = ventaRepo;
    }

    //Método propio para buscar los supuestos productos que pertenecen a la venta
    private List<ProductoIntegrationDto> findProductos(List<Long> productosIds){
        //Si no hay productos a buscar, no tiene sentido buscar nada -> Excepción
        if(productosIds.isEmpty()){throw new ProductoNotFoundException("No se solicitó la información de ningún producto");}

        //Si solo hay un objeto en la lista, hacemos la búsqueda por findProducto(id) y formamos la lista para retornar
        if(productosIds.size() == 1){return List.of(productoIntegration.findProducto(productosIds.get(0)));}

        /*Intentamos hacer la búsqueda de los productos pasando por la capa de integración con producto-service
        donde se manejarán todas las excepcione relacionadas con esta comunicación*/
        return productoIntegration.findProductos(new HashSet<>(productosIds));
    }

    //Método propio para buscar al cliente dueño de una determinada venta
    private ClienteIntegrationDto findCliente(Long id){

        /*Intentamos hacer la búsqueda del cliente pasando por la capa de integración con cliente-service
        donde se manejarán todas las excepcione relacionadas con esta comunicación*/
        return clienteIntegration.findCliente(id);
    }

    /*Método propio para preparar al Cliente que viene desde cliente-service para que sea persistido como parte de la
     Venta en la base de datos*/
    private ClienteSnapshot clienteIntegrationToSnapshot(ClienteIntegrationDto clienteIntegrado){
        return new ClienteSnapshot(clienteIntegrado.getId(), clienteIntegrado.getName(), clienteIntegrado.getCellphone(),
                clienteIntegrado.getDocument(), clienteIntegrado.getAddress());
    }

    //Método propio para sacar los ids de los productos de una lista de objetos ProductoRequestDto
    /*Recordemos qu este tipo de objetos tienen el id y la cantidad del producto que se desea consultar,
     pero para hacer la petición a producto-service solo necesitamos los ids, por lo que los apartamos*/
    private List<Long> sacarProductosIds(List<ProductoRequestDto> listProductos){
        //Lista de los ids de los productos que se están solicitando encontrar
        List<Long> productosIds = new ArrayList<>();

        //Agregamos cada ID a la lista productosIds para su posterior búsqueda
        for(ProductoRequestDto productoRequest: listProductos){productosIds.add(productoRequest.getId());}

        return productosIds;
    }

    //Método propio para verificar si realmente llegaron completos los productos que fueron mandados a buscar
    private void verificarCargaCompletaDeProductos(List<ProductoIntegrationDto> productosIntegrados, List<Long> productosIds){

        /*El tamaño de la lista de productos que llega una vez se haga la petición DEBE ser igual al tamaño
        de la lista de ids que se mandó a buscar, de lo contrario quiere decir que hubo productos que no fueron
        encontrados -> Excepción indicando el problema*/
        //Se parsea la colección de ids de List<> a Set<> para eliminar duplicados
        if(productosIntegrados.size() < (new HashSet<>(productosIds)).size()){throw new ProductoNotFoundException("Uno o más productos no fueron encontrados");}
    }

    /*Método propio para construir los DTO que viajarán en la integración con producto-service para hacer una operación
     (validar, descontar, etc) sobre el stock de los diferentes productos */
    private List<ProductoIntegrationStockDto> productosRequestToIntegration(List<ProductoRequestDto> productosRequest){
        //Lista de productos que van a viajar en la petición a producto-service con la cantidad que se va a operar
        List<ProductoIntegrationStockDto> productosIntegration = new ArrayList<>();

        //Vamos creando los DTO de los productos a integrar a partir de los datos de los productos que mandó el cliente
        for(ProductoRequestDto productoValidar: productosRequest){
            productosIntegration.add(new ProductoIntegrationStockDto(productoValidar.getId(),
                    productoValidar.getQuantity()));
        }

        //Retornamos lista de productos lista (ready) para integración
        return productosIntegration;
    }

    /*Método propio que construye los DTO que viajan en la integración con producto-service para reponer el stock
        de la lista de productos que se envíen como Snapshot*/
    private List<ProductoIntegrationStockDto> productosSnapshotToIntegration(Set<ProductoSnapshot> productosSnapshot){
        //Lista que va a almacenar los DTO de integración
        List<ProductoIntegrationStockDto> productosIntegration = new ArrayList<>();

        //Recorremos la lista de ProductoSnapshot para construir los DTO de integración a partir de los datos de estos
        for(ProductoSnapshot productoSnapshot: productosSnapshot){
            productosIntegration.add(
                    //Creamos instancia de los DTO de integración y agregamos a la lista
                    //En este caso se va a reponer la cantidad que se compró de cada producto
                    new ProductoIntegrationStockDto(productoSnapshot.getProductId(), productoSnapshot.getPurchasedQuantity())
            );
        }

        return productosIntegration;
    }


    /*Método propio que hace la integración con producto-service para validar si el stock de una lista de productos es
     * suficiente para la cantidad que se quiere comprar de estos*/
    private void verificarProductosStock(List<ProductoRequestDto> productosValidarStock){
        //Usamos el método propio para construir la lista de DTO a partir de los datos de los productos que manda el cliente para comprar
        List<ProductoIntegrationStockDto> productosIntegrar = productosRequestToIntegration(productosValidarStock);

        //Hacemos la integración
        productoIntegration.validarProductosStock(productosIntegrar);
    }

    //Método propio que integra a producto-service para usar el método que descuenta una cierta cantidad al stock de una lista de productos
    private void descontarProductosStock(List<ProductoRequestDto> productosRequest){
        //Construimos los DTO de integración a partir de los datos de los productos a los que se va a descontar
        List<ProductoIntegrationStockDto> productosIntegration = productosRequestToIntegration(productosRequest);

        //Con los DTO hacemos la integración y descontamos
        productoIntegration.descontarProductosStock(productosIntegration);
    }

    //Método propio para llamar al método de integración que repone una cierta cantidad de stock a cada producto una lista de productos
    private void reponerProductosStock(Set<ProductoSnapshot> listProductos) {
        //Construimos la lista que va a almacenar los DTO de integración que van a viajar en la petición a producto-stock
        List<ProductoIntegrationStockDto> productosIntegration = productosSnapshotToIntegration(listProductos);

        //Reponemos stock a cada producto que fue traído en la colección
        productoIntegration.reponerProductosStock(productosIntegration);
    }

    /*Método propio para transferir los datos de una determinada lista de productos que vengan en la petición como DTO
         para su posterior persistencia en la base de datos*/
    private List<ProductoSnapshot> productosIntegrationToSnapshot(List<ProductoRequestDto> productosRequest, List<ProductoIntegrationDto> productosIntegration){
        //Validamos que el stock de cada producto es suficiente para cubrir la cantidad que se quiere comprar
        verificarProductosStock(productosRequest);

        //Lista que va a almacenar los diferentes productos una vez estén listos para ser persistidos en la base de datos (Snapshots)
        List<ProductoSnapshot> productosSnapshot = new ArrayList<>();

        /*Ahora vamos a encontrar los objetos que coincidan entre los que vinieron del servicio Producto (Integrados)
        y los que se mandaron a buscar desde el cliente (ProductoRequestDto) para construir el Snapshot a partir de ambos*/

        //Recorremos la lista de los productos que vinieron en la petición a producto-service (integrados)
        for(ProductoIntegrationDto objIntegration: productosIntegration){

            //Y vamos recorriendo la lista de los productosRequest hasta encontrar el que coincida en ID con cada producto integrado
            for(ProductoRequestDto objRequest: productosRequest){
                if(objIntegration.getId().equals(objRequest.getId())){

                    //Se calcula el subTotal de cada producto comprado en formato BigDecimal
                    BigDecimal subTotal = objIntegration.getPrice().multiply(BigDecimal.valueOf(objRequest.getQuantity()));

                    //Una vez pasados los filtros anteriores, podemos crear y agregar el Snapshot a la lista de Snapshots finales
                    productosSnapshot.add(new ProductoSnapshot(objIntegration.getId(), objIntegration.getName(), objIntegration.getPrice(),
                            objRequest.getQuantity(), subTotal, objIntegration.getDescription()));

                    //Pasamos al siguiente Producto Integrado y repetimos proceso
                    break;
                }
            }
        }

        /*Si no hay ningún problema en la creación de los Snapshot que se van a persistir en la base de datos, descontamos
         la respectiva cantidad en producto-service al stock de cada producto que se compró */
        descontarProductosStock(productosRequest);

        //Finalmente, retornamos lista de ProductosSnapshot
        return productosSnapshot;

    }

    //Método propio para preparar la exposición de una lista de productos al cliente (view)
    private List<ProductoResponseDto> productosSnapshotToResponse(List<ProductoSnapshot> productosSnapshot){
        //Lista de productos para respuesta (response)
        List<ProductoResponseDto> productosResponse = new ArrayList<>();

        //Vamos recorriendo la lista de Snapshots y sacando los objetos ProductoResponseDto
        for(ProductoSnapshot objSnapshot: productosSnapshot){
            //Se va llenando la lista de Response
            productosResponse.add(new ProductoResponseDto(objSnapshot.getProductId(), objSnapshot.getProductName(),
                    objSnapshot.getProductPrice(), objSnapshot.getPurchasedQuantity(), objSnapshot.getSubTotal(),
                      objSnapshot.getProductDescription()));
        }

        //Retorno
        return productosResponse;
    }

    //Método propio para preparar la exposición de un cliente como Response de la petición
    private ClienteResponseDto clienteSnapshotToResponse(ClienteSnapshot objCliente){
        //Retornamos instancia de ClienteResponseDto
        return new ClienteResponseDto(objCliente.getClientId(), objCliente.getClientName(), objCliente.getClientCellphone(),
                objCliente.getClientDocument(), objCliente.getClientAddress());
    }

    //Método propio para construir una Venta que viaje como Response a partir de una que vino desde la base de datos
    private VentaResponseDto buildVentaResponse(Venta objVenta){
        //Se saca el objeto de VentaResponse
        VentaResponseDto objVentaResponse = new VentaResponseDto();

        //Carga de datos
        objVentaResponse.setId(objVenta.getId());
        objVentaResponse.setDate(objVenta.getDate());
        objVentaResponse.setTotalItems(objVenta.getTotalItems());
        objVentaResponse.setTotalPrice(objVenta.getTotalPrice());
        //Método propio para preparar productos
        objVentaResponse.setProductsList(productosSnapshotToResponse(new ArrayList<>(objVenta.getListProducts())));
        //Método propio para preparar cliente
        objVentaResponse.setClient(clienteSnapshotToResponse(objVenta.getClient()));
        objVentaResponse.setStatus(objVenta.getStatus());

        //Retorno de venta
        return objVentaResponse;
    }

    //Método propio para calcular el valor total de una venta a partir de los subtotales de sus productos
    private BigDecimal calcularTotalPrice(List<ProductoSnapshot> productosComprados){
        BigDecimal totalPrice = BigDecimal.ZERO;

        for(ProductoSnapshot objSnapshot: productosComprados){
            totalPrice = totalPrice.add(objSnapshot.getSubTotal());
        }

        return  totalPrice;
    }

    //Método propio para calcular la cantidad total de productos comprados
    private Integer calcularTotalItems(List<ProductoSnapshot> productosComprados){
        Integer totalItems = 0;

        for(ProductoSnapshot objSnapshot: productosComprados){
            totalItems += objSnapshot.getPurchasedQuantity();
        }

        return totalItems;
    }


    //Método propio para construir una Venta que será persistida en la base de datos a partir de una venta proporcionada por el cliente (view)
    private Venta buildVentaPersistir(List<ProductoRequestDto> productsList){

        //Busca el cliente autenticado dueño de la venta
        ClienteIntegrationDto cliente = findCliente(
                getAuthenticatedClientId()
        );
        //Preparamos Cliente para persistencia
        ClienteSnapshot clienteSnapshot = clienteIntegrationToSnapshot(cliente);

        //Ahora se saca la lista de los ids de los productos que se están solicitando encontrar (Lista de productos en objRequest)
        List<Long> productosIds = sacarProductosIds(productsList);

        //Luego se buscan los productos a partir de la lista anterior de ids
        List<ProductoIntegrationDto> productosIntegration = findProductos(productosIds);

        //Comprobamos la correcta carga de TODOS los productos en la lista "productosIntegration"
        verificarCargaCompletaDeProductos(productosIntegration, productosIds);

        /*Una vez confirmado que todos los productos llegaron, procedemos a prepararlos para su persistencia en la
         base de datos, pasando de productos integrados a productos Snapshot*/
        List<ProductoSnapshot> productosSnapshot = productosIntegrationToSnapshot(productsList, productosIntegration);


        //Creamos instancia con todos los datos y retornamos
        return(
                new Venta(
                    null, //El id no sé manda OBVIAMENTE
                    LocalDate.now(),
                    calcularTotalItems(productosSnapshot),
                    calcularTotalPrice(productosSnapshot),
                    new HashSet<>(productosSnapshot), //Colección de productos
                    clienteSnapshot, //Cliente de venta
                    VentaStatus.PENDING //Inicialmente, se marca la venta como pendiente
                )
        );
    }

    //Extrae la identidad de cliente autenticado y retorna su id
    private Long getAuthenticatedClientId(){
        //Busca objeto con la información del token JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Saca objeto Principal con todos los claims almacenamos en el token
        Jwt principal = (Jwt) authentication.getPrincipal();

        //Busca identidad de negocio del usuario
        Number clientId = principal.getClaim("clientId");

        //Valida que el usuario realmente sea cliente
        if(clientId == null){throw new UnauthorizedOperationException("El usuario no es cliente, por lo que " +
                "no puede realizar la operación");
        }

        return  clientId.longValue();

    }


    //Método propio para buscar una venta desde la base de datos para operaciones internas
    @Transactional(readOnly = true)
    protected Venta findVenta(Long id){
        Optional<Venta> objVenta =  ventaRepo.findById(id);

        //Optional vacío = Venta no existe --> Excepción VentaNotFound
        if(objVenta.isEmpty()){throw new VentaNotFoundException("No se encontró Venta con id: " + id);}

        return objVenta.get();
    }

    @Transactional(readOnly = true)
    @Override
    public List<VentaResponseDto> findAllVentas() {
        //Lista de ventas para la Response
        List<VentaResponseDto> listVentas = new ArrayList<>();

        //Vamos preparando cada venta para ser expuesta al cliente
        for(Venta objVenta: ventaRepo.findAll()){
            listVentas.add(buildVentaResponse(objVenta));
        }

        //Retornamos ventas
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

        //A partir de la venta enviada por el cliente, construimos Venta para persistir
        Venta objVenta = buildVentaPersistir(productsList);

        //Antes de registrar, se marca la venta como completada
        objVenta.setStatus(VentaStatus.COMPLETED);

        //Guardamos registro
        ventaRepo.save(objVenta);

        //Mostramos lo registrado
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

    @Transactional(readOnly = true)
    @Override
    public List<VentaResponseDto> findClienteVentas(Long clientId) {

        //Si no existe el cliente, lanzamos la excepción
        findCliente(clientId);

        //Lista de las ventas que comparten un determinado cliente
        List<VentaResponseDto> listVentas = new ArrayList<>();

        //Recorremos cada venta que se encontró que tiene cliente con Id=clienteId y la vamos preparando para exponerla en la vista
        for(Venta objVenta: ventaRepo.findByClient_clientId(clientId)){
            //Preparamos y agregamos a lista final
            listVentas.add(buildVentaResponse(objVenta));
        }

        return listVentas;
    }
}
