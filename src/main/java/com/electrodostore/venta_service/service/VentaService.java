package com.electrodostore.venta_service.service;

import com.electrodostore.venta_service.dto.*;
import com.electrodostore.venta_service.exception.ProductoNotFoundException;
import com.electrodostore.venta_service.integration.ClienteIntegrationService;
import com.electrodostore.venta_service.integration.ProductoIntegrationService;
import com.electrodostore.venta_service.model.ProductoSnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class VentaService implements IVentaService{

    //Inyección de dependencia para la integración con producto-service
    //Inyección de dependencia para la integración con cliente-service
    private final ClienteIntegrationService clienteIntegration;
    private final ProductoIntegrationService productoIntegration;
    //Inyección por constructor
    public VentaService(ProductoIntegrationService productoIntegration, ClienteIntegrationService clienteIntegration){
        this.productoIntegration = productoIntegration;
        this.clienteIntegration = clienteIntegration;
    }

    //Método propio para buscar los supuestos productos que pertenecen a la venta
    private List<ProductoIntegrationDto> findProductos(List<Long> productosIds){
        //Si no hay productos a buscar, no tiene sentido buscar nada -> Excepción
        if(productosIds.isEmpty()){throw new ProductoNotFoundException("No se solicitó la información de ningún producto");}

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
    private List<ProductoSnapshot> ProductosDtoToSnapshot(List<ProductoRequestDto> productosRequest){
        //Lista de los ids de los productos que se están solicitando encontrar
        List<Long> productosIds = sacarProductosIds(productosRequest);

        //Lista de los productos con sus campos completos después de ser buscados por sus ids
        List<ProductoIntegrationDto> productosIntegration = findProductos(productosIds);

        //Comprobamos la correcta carga de TODOS los productos
        verificarCargaCompletaDeProductos(productosIntegration, productosIds);

        //Lista que va a almacenar los diferentes productos una vez estén listos para ser persistidos en la base de datos (Snapshots)
        List<ProductoSnapshot> productosSnapshot = new ArrayList<>();

        /*Ahora vamos a encontrar los objetos que coincidan entre los que vinieron del servicio Producto (Integrados)
        y los que se mandaron a buscar desde el cliente (ProductoRequestDto) para construir el Snapshot a partir de ambos*/

        //Recorremos la lista de los productos que vinieron en la petición a producto-service (integrados)
        for(ProductoIntegrationDto objIntegration: productosIntegration){

            //Y vamos recorriendo la lista de los productosRequest hasta encontrar el que coincida en ID con cada producto integrado
            for(ProductoRequestDto objRequest: productosRequest){
                if(objIntegration.getId().equals(objRequest.getId())){

                    //Una vez encontremos coincidencia, debemos verificar si hay stock disponible para cubrir la cantidad comprada
                    if(objRequest.getQuantity() > objIntegration.getStock()){
                        //Si el stock no es suficiente -> Excepción
                        throw new ProductoNotFoundException("El producto con id: " + objIntegration.getId() + " no tiene suficiente stock disponible para la venta");
                    }

                    //Una vez pasados los filtros anteriores, podemos crear y agregar el Snapshot a la lista de Snapshots finales
                    productosSnapshot.add(new ProductoSnapshot(objIntegration.getId(), objIntegration.getName(), objRequest.getQuantity(),
                            objIntegration.getPrice()*objRequest.getQuantity(), objIntegration.getDescription()));

                    //Descontamos la cantidad comprada al producto en el servicio Productos
                    productoIntegration.descontarProductoStock(objIntegration.getId(), objRequest.getQuantity());
                }
                //Pasamos al siguiente Producto Integrado y repetimos proceso
                break;
            }
        }

        //Finalmente, retornamos lista de ProductosSnapshot
        return productosSnapshot;

    }

    @Override
    public List<VentaResponseDto> findAllVentas() {
        return List.of();
    }

    @Override
    public VentaResponseDto findVenta(Long id) {
        return null;
    }

    @Override
    public VentaResponseDto saveVenta(VentaRequestDto objNuevo) {
        return null;
    }

    @Override
    public void deleteVenta(Long id) {

    }

    @Override
    public VentaResponseDto updateVenta(Long id, VentaRequestDto objUpdated) {
        return null;
    }

    @Override
    public VentaResponseDto patchVenta(Long id, VentaRequestDto objUpdated) {
        return null;
    }
}
