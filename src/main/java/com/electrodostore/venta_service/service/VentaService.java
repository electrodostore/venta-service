package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.*;
import com.electrodostore.venta_service.exception.ClienteNotFoundException;
import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.exception.VentaNotFoundException;
import com.electrodostore.venta_service.integration.cliente.ClienteIntegrationService;
import com.electrodostore.venta_service.integration.cliente.dto.ClienteIntegrationDto;
import com.electrodostore.venta_service.integration.producto.ProductoIntegrationService;
import com.electrodostore.venta_service.integration.producto.dto.ProductoIntegrationDto;
import com.electrodostore.venta_service.model.ClienteSnapshot;
import com.electrodostore.venta_service.model.ProductoSnapshot;
import com.electrodostore.venta_service.model.Venta;
import com.electrodostore.venta_service.repository.IVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    /*Método propio para transferir los datos de una determinada lista de productos que vengan en la petición como DTO
         para su posterior persistencia en la base de datos*/
    private List<ProductoSnapshot> productosIntegrationToSnapshot(List<ProductoRequestDto> productosRequest, List<ProductoIntegrationDto> productosIntegration){
        //Lista que va a almacenar los diferentes productos una vez estén listos para ser persistidos en la base de datos (Snapshots)
        List<ProductoSnapshot> productosSnapshot = new ArrayList<>();

        /*Ahora vamos a encontrar los objetos que coincidan entre los que vinieron del servicio Producto (Integrados)
        y los que se mandaron a buscar desde el cliente (ProductoRequestDto) para construir el Snapshot a partir de ambos*/

        //Recorremos la lista de los productos que vinieron en la petición a producto-service (integrados)
        for(ProductoIntegrationDto objIntegration: productosIntegration){

            //Y vamos recorriendo la lista de los productosRequest hasta encontrar el que coincida en ID con cada producto integrado
            for(ProductoRequestDto objRequest: productosRequest){
                if(objIntegration.getId().equals(objRequest.getId())){

                    //Descontamos la cantidad comprada al producto en el servicio Productos
                    productoIntegration.descontarProductoStock(objIntegration.getId(), objRequest.getQuantity());

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

    //Método propio para llamar al método de integración que repone una cierta cantidad de stock a cada producto de la colección
    private void reponerProductosStock(Set<ProductoSnapshot> listProductos){

        //Reponemos stock a cada producto que fue traído en la colección
        for(ProductoSnapshot objSnapshot: listProductos){
            productoIntegration.reponerProductoStock(objSnapshot.getProductId(), objSnapshot.getPurchasedQuantity());
        }
    }

    //Método propio para construir una Venta que será persistida en la base de datos a partir de una venta proporcionada por el cliente (view)
    private Venta buildVentaPersistir(VentaRequestDto objRequest){

        //Una venta no puede existir sin un cliente
        if(objRequest.getClientId() == null){throw new ClienteNotFoundException("No fue asignado ningún cliente a la venta");}

        //Primero se saca la lista de los ids de los productos que se están solicitando encontrar (Lista de productos en objRequest)
        List<Long> productosIds = sacarProductosIds(objRequest.getProductsList());

        //Luego se buscan los productos a partir de la lista anterior de ids
        List<ProductoIntegrationDto> productosIntegration = findProductos(productosIds);

        //Comprobamos la correcta carga de TODOS los productos en la lista "productosIntegration"
        verificarCargaCompletaDeProductos(productosIntegration, productosIds);

        /*Una vez confirmado que todos los productos llegaron, procedemos a prepararlos para su persistencia en la
         base de datos, pasando de productos integrados a productos Snapshot*/
        List<ProductoSnapshot> productosSnapshot = productosIntegrationToSnapshot(objRequest.getProductsList(), productosIntegration);

        //Buscamos el cliente dueño de la venta
        ClienteIntegrationDto cliente = findCliente(objRequest.getClientId());
        //Preparamos Cliente para persistencia
        ClienteSnapshot clienteSnapshot = clienteIntegrationToSnapshot(cliente);

        //Creamos instancia con todos los datos y retornamos
        return(
                new Venta(
                    null, //El id no sé manda OBVIAMENTE
                    objRequest.getDate(),
                    calcularTotalItems(productosSnapshot),
                    calcularTotalPrice(productosSnapshot),
                    new HashSet<>(productosSnapshot), //Colección de productos
                    clienteSnapshot //Cliente de venta
                )
        );
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
    public VentaResponseDto saveVenta(VentaRequestDto objNuevo) {

        //A partir de la venta enviada por el cliente, construimos Venta para persistir
        Venta objVenta = buildVentaPersistir(objNuevo);

        //Guardamos registro
        ventaRepo.save(objVenta);

        //Mostramos lo registrado
        return buildVentaResponse(objVenta);
    }

    @Transactional
    @Override
    public void deleteVenta(Long id) {
        Venta objVenta = findVenta(id);

        //Reponemos al stock de cada producto la cantidad que fue comprada
        reponerProductosStock(objVenta.getListProducts());

        ventaRepo.delete(objVenta);

    }

    @Transactional
    @Override
    public VentaResponseDto updateVenta(Long id, VentaRequestDto objUpdated) {

        //No se puede actualizar Venta sin un cliente
        if(objUpdated.getClientId() == null){throw new ClienteNotFoundException("No se puede actualizar la venta si no es proporcionado un cliente");}

        //Buscamos venta y si no existe, lo sabremos
        Venta objVenta =  findVenta(id);

        //Construcción de nueva venta a partir de los datos enviados por el cliente (objUpdated)
        Venta ventaUpdated = buildVentaPersistir(objUpdated);

        /*Si los datos actualizados se crean sin ninguna excepción, significa que los productos nuevos solicitados para la
         nueva lista son válidos por lo que reponemos el stock de los productos antiguos de la venta*/
        reponerProductosStock(objVenta.getListProducts());
        //Vaciamos lista antigua de productos para darle paso a la nueva
        objVenta.getListProducts().clear();

        //Ahora, se actualizan los datos de la venta antigua por los de la nueva venta
        objVenta.setDate(objUpdated.getDate());
        objVenta.setTotalItems(ventaUpdated.getTotalItems());
        objVenta.setTotalPrice(ventaUpdated.getTotalPrice());
        objVenta.setListProducts(ventaUpdated.getListProducts());
        objVenta.setClient(ventaUpdated.getClient());

        //Actualizamos en DB
        ventaRepo.save(objVenta);

        return buildVentaResponse(objVenta);
    }

    @Transactional
    @Override
    public VentaResponseDto patchVenta(Long id, VentaRequestDto objUpdated) {
        //Buscamos venta para verificar si existe
        Venta objVenta = findVenta(id);

        //Si el cliente solicita cambiar fecha, se cambia
        if(objUpdated.getDate() != null){objVenta.setDate(objUpdated.getDate());}

        //Si manda una otra lista de productos, procedemos a actualizar la anterior de la venta
        if(!objUpdated.getProductsList().isEmpty()){

            //Se saca la lista de los ids de los productos que se están solicitando encontrar (Lista de productos en objUpdated)
            List<Long> productosIds = sacarProductosIds(objUpdated.getProductsList());

            //Luego se buscan los productos a partir de la lista anterior de ids
            List<ProductoIntegrationDto> productosIntegration = findProductos(productosIds);

            //Comprobamos la correcta carga de TODOS los productos en la lista "productosIntegration"
            verificarCargaCompletaDeProductos(productosIntegration, productosIds);

            /*Ahora, procedemos a preparar los productos integrados para su persistencia en la base de datos, pasando de
             productosIntegration a productosSnapshot, aquí también se verifica si el stock de los productos nuevos
             es suficiente para la cantidad que se está comprando*/
            List<ProductoSnapshot> productosSnapshot = productosIntegrationToSnapshot(objUpdated.getProductsList(), productosIntegration);

            /*Cuándo se confirme que hay productos VÁLIDOS en la nueva lista de productos buscados -> "productosIntegration",
             que todos los productos solicitados llegaron, y que el stock de los nuevos productos es suficiente.
             Procedemos a reponer el stock de los productos de la venta que serán reemplazados (Los antiguos)*/
            /*NOTA: Si no hay productos válidos en la nueva lista o algún producto solicitado no existe, o no hay stock
            suficiente en los nuevos productos. No se llegará a este punto, ya que se sabría en los filtros anteriores
            y en la búsqueda de "productosIntegration"*/
            reponerProductosStock(objVenta.getListProducts());
            //Vaciamos lista antigua de productos para darle paso a la nueva
            objVenta.getListProducts().clear();

            //Cambiamos total de productos y precio total de la venta con respecto a los productos nuevos
            objVenta.setTotalItems(calcularTotalItems(productosSnapshot));
            objVenta.setTotalPrice(calcularTotalPrice(productosSnapshot));

            //Finalmente, agregamos la nueva lista de productos a la venta (objVenta) lista para persistir
            objVenta.setListProducts(new HashSet<>(productosSnapshot));
        }

        //Si hay una actualización de cliente --> se hace sin problema
        if(objUpdated.getClientId() != null){

            //Búsqueda de cliente por ID en cliente-service
            ClienteIntegrationDto objCliente = findCliente(objUpdated.getClientId());

            //Nuevo cliente listo para ser persistido en la DB
            ClienteSnapshot clienteSnapshot = clienteIntegrationToSnapshot(objCliente);

            //Se lo asignamos a la venta
            objVenta.setClient(clienteSnapshot);
        }

        //Finalmente, se guardan los cambios
        ventaRepo.save(objVenta);

        //Se retorna la venta actualizada
        return buildVentaResponse(objVenta);
    }
}
