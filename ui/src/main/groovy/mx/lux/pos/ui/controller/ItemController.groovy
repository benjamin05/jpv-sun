package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Articulo
import mx.lux.pos.model.DetalleNotaVenta
import mx.lux.pos.model.Diferencia
import mx.lux.pos.model.InventarioFisico
import mx.lux.pos.model.MontoGarantia
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.model.Sucursal
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.ArticuloService
import mx.lux.pos.service.DetalleNotaVentaService
import mx.lux.pos.service.NotaVentaService
import mx.lux.pos.service.TicketService
import mx.lux.pos.ui.model.Differences
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.view.dialog.ChargeDialog
import mx.lux.pos.ui.view.dialog.DifferencesDialog
import mx.lux.pos.ui.view.dialog.WaitDialog
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import mx.lux.pos.model.QArticulo
import mx.lux.pos.ui.view.dialog.ImportPartMasterDialog
import javax.swing.JOptionPane
import javax.swing.JDialog
import mx.lux.pos.service.business.Registry

import java.text.NumberFormat

@Slf4j
@Component
class ItemController {

  private static final String MSJ_ARCHIVO_GENERADO = 'El archivo de inventario fue cargado correctamente'
  private static final String TXT_ARCHIVO_GENERADO = 'Archivo de Inventario'
  private static final String MSJ_ARCHIVO_NO_GENERADO = 'No se genero correctamente el archivo de inventario'
  private static final String TXT_DIFERENCIAS = 'Diferencias'
  private static final String MSJ_DIFERENCIAS_NO_RECIBIDAS = 'No se recibieron correctamente las diferencias'

  private static final String MSJ_INV_FISICO_NO_ENVIADO = 'No se han podido enviar los datos del inventario'
  private static final String MSJ_INV_FISICO_ENVIADO = 'Se han enviado correctamente los datos del inventario'

  private static final String MSJ_INV_FISICO_NO_INICIALIZADO = 'No se han podido inicializar el inventario'
  private static final String MSJ_INV_FISICO_INICIALIZADO = 'Se ha inicializado correctamente el inventario'

  private static ArticuloService articuloService
  private static TicketService ticketService
  private static DetalleNotaVentaService detalleNotaVentaService
  private static NotaVentaService notaVentaService

  @Autowired
  public ItemController( ArticuloService articuloService, TicketService ticketService, DetalleNotaVentaService detalleNotaVentaService,
                         NotaVentaService notaVentaService ) {
    this.articuloService = articuloService
    this.ticketService = ticketService
    this.detalleNotaVentaService = detalleNotaVentaService
    this.notaVentaService = notaVentaService
  }

  static Item findItem( Integer id ) {
    log.debug( "obteniendo articulo con id: ${id}" )
    Item.toItem( articuloService.obtenerArticulo( id ) )
  }

  static List<Item> findItems( String code ) {
    log.debug( "buscando articulos con articulo: ${code}" )
    def results = articuloService.listarArticulosPorCodigo( code )
    results.collect {
      Item.toItem( it )
    }
  }

  static List<Item> findItemsLike( String input ) {
    log.debug( "buscando articulos con articulo similar a: $input" )
    def results = articuloService.listarArticulosPorCodigoSimilar( input )
    results.collect {
      Item.toItem( it )
    }
  }

  static List<Item> findItemsByQuery( final String query ) {
    log.debug( "buscando de articulos con query: $query" )
    if ( StringUtils.isNotBlank( query ) ) {
      List<Articulo> items = findPartsByQuery( query )
      if (items.size() > 0) {
        log.debug( "Items:: ${items.first()?.dump()} " )
        return items?.collect { Item.toItem( it ) }
      }
    }
    return [ ]
  }

  static List<Articulo> findPartsByQuery( final String query ) {
    return findPartsByQuery( query, true )
  }

  static List<Articulo> findPartsByQuery( final String query, Boolean incluyePrecio ) {
    List<Articulo> items = [ ]
    if ( StringUtils.isNotBlank( query ) ) {
      if ( query.integer ) {
        log.debug( "busqueda por id exacto ${query}" )
        Articulo articulo = articuloService.obtenerArticulo( query.toInteger(), incluyePrecio )
        if( articulo != null ){
          items.add( articulo )
        }
      } else {
        def anyMatch = '*'
        def colorMatch = ','
        def typeMatch = '+'
        if ( query.contains( anyMatch ) ) {
          def tokens = query.tokenize( anyMatch )
          def code = tokens?.first() ?: null
          log.debug( "busqueda con codigo similar: ${code}" )
          items = articuloService.listarArticulosPorCodigoSimilar( code, incluyePrecio ) ?: [ ]
        } else {
          def tokens = query.replaceAll( /[+|,]/, '|' ).tokenize( '|' )
          def code = tokens?.first() ?: null
          log.debug( "busqueda con codigo exacto: ${code}" )
          items = articuloService.listarArticulosPorCodigo( code, incluyePrecio ) ?: [ ]
        }
        if ( query.contains( colorMatch ) ) {
          String color = query.find( /\,(\w+)/ ) { m, c -> return c }
          log.debug( "busqueda con color: ${color}" )
          items = items.findAll { it?.codigoColor?.equalsIgnoreCase( color ) }
        }
        if ( query.contains( typeMatch ) ) {
          String type = query.find( /\+(\w+)/ ) { m, t -> return t }
          log.debug( "busqueda con tipo: ${type}" )
          items = items.findAll { it?.idGenerico?.equalsIgnoreCase( type ) }
        }
      }
    }
    return items
  }

    static List<Item> findItemByArticleAndColor( String query, String color  ) {
        log.debug( "buscando de un articulo con query: $query" )
        if ( StringUtils.isNotBlank( query ) ) {

            List<Articulo> items = new ArrayList<Articulo>()
            try{
            items = articuloService.findArticuloyColor( query, color )
            } catch( Exception e ){
                System.out.println( e )
            }
            return items?.collect { Item.toItem( it ) }
        }
        return [ ]
    }

  static String getManualPriceTypeList( ) {
    String list = articuloService.obtenerListaGenericosPrecioVariable()
    log.debug( "Determina la lista de Genericos precio variable: ${ list } " )
    return list
  }


  static void generateInventoryFile( ){
    log.debug( "generateInventoryFile( )" )
    Boolean archGenerado = articuloService.generarArchivoInventario()
    if( archGenerado ){
      JOptionPane.showMessageDialog( new JDialog(), String.format(MSJ_ARCHIVO_GENERADO, Registry.archivePath), TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    } else {
      JOptionPane.showMessageDialog( new JDialog(), MSJ_ARCHIVO_NO_GENERADO, TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    }
  }


  static void sendInventoryFile( ){
      log.debug( "sendInventoryFile( )" )
      Boolean archGenerado = articuloService.enviarInventario()
      if( archGenerado ){
          JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_ENVIADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
      } else {
          JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_NO_ENVIADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
      }
  }


  static void receivedDifferencesFile(  ){
      /*log.debug( "receivedDifferencesFile( )" )
      Boolean recibidos = articuloService.recibeDiferencias( )
      if( recibidos ){*/
          DifferencesDialog dialog = new DifferencesDialog( )
          dialog.show()
      /*} else {
          JOptionPane.showMessageDialog( new JDialog(), MSJ_DIFERENCIAS_NO_RECIBIDAS, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
      }*/
  }

  static List<Diferencia> findAllDifferences( ) {
    log.debug('findAllDifferences( )')
    /*List<Diferencia> lstDifferences = articuloService.obtenerDiferencias()
    lstDifferences.collect {
        Differences.toDifferences( it )
    }*/
    return articuloService.obtenerDiferencias()
  }

  static void printDifferences( ){
    ticketService.imprimeDiferencias()
  }


  static Boolean generateDifferencesFile( ){
    return articuloService.generarArchivoDiferencias()
  }

  static void generatePhysicalInventoryFile( ){
      log.debug( "generatePhysicalInventoryFile( )" )
      Boolean archGenerado = articuloService.generarArchivoInventarioFisico()
      if( archGenerado ){
          JOptionPane.showMessageDialog( new JDialog(), String.format("Se ha inicializado correctamente el inventario", Registry.archivePath), "Inventario SOI", JOptionPane.INFORMATION_MESSAGE )
      } else {
          JOptionPane.showMessageDialog( new JDialog(), "No se ha inicializado correctamente el inventario", "Inventario SOI", JOptionPane.INFORMATION_MESSAGE )
      }
  }

  static Boolean generatePhysicalDiferencesInventory( ){
    log.debug( "generatePhysicalInventoryFile( )" )
    Boolean archivoCargado = false
    List<InventarioFisico> lstInventario = new ArrayList<>()
    WaitDialog dialog = new WaitDialog( "Inventario", "<html>Cargando archivo de inventario Fisico. Espere un momento</html>" )
    Runnable runnable = new Runnable() {
      void run(){
        lstInventario = articuloService.cargaArchivoInventarioFisico()
        if( lstInventario.size() > 0 ){
          archivoCargado = articuloService.generaDiferencias( lstInventario )
          articuloService.difArticulosNoInv()
          archivoCargado = articuloService.cargaDiferencias( )
          dialog.dispose()
        } else {
          dialog.dispose()
        }
      }
    }
    Thread t = new Thread( runnable )
    t.start();
    dialog.show()
    if( archivoCargado ){
      JOptionPane.showMessageDialog( new JDialog(), String.format(MSJ_ARCHIVO_GENERADO, Registry.archivePath), TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    } else {
      JOptionPane.showMessageDialog( new JDialog(), MSJ_ARCHIVO_NO_GENERADO, TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    }
    return archivoCargado
  }

  static void printPhysicalDiferencesInventory( ){
    log.debug( "printPhysicalDiferencesInventory( )" )
    ticketService.imprimeDiferencias()
  }

  static Boolean isInventoried( Integer idArticulo ){
    log.debug( "isInventoried( )" )
    Boolean esInventariable = true
    Articulo articulo = articuloService.obtenerArticulo( idArticulo )
    if(articulo != null){
      if(!articulo.generico.inventariable){
        esInventariable = false
      }
    }
    return esInventariable
  }


  static Item findArticleMinPrice( List<Integer> idsArticulo ){
    Articulo articulo = articuloService.buscaArticuloMenorPrecio( idsArticulo )
    return Item.toItem( articulo )
  }


  static Boolean areArticlesSamePrice( List<Integer> idsArticulo ){
    Boolean samePrice = articuloService.tienenArticuloMsimoPrecio( idsArticulo )
    return samePrice
  }


  static Boolean initializingInventory( ){
    log.debug( "initializingInventory( )" )
    Boolean inicializado = false
    inicializado = articuloService.inicializarInventario()
    if( inicializado ){
      JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_INICIALIZADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
    } else {
      JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_NO_INICIALIZADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
    }
    return inicializado
  }



  static BigDecimal warrantyValid( BigDecimal priceItem, Integer idWarranty ){
    BigDecimal warrantyAmount = BigDecimal.ZERO
    Articulo warranty = articuloService.obtenerArticulo( idWarranty, true )
    if( warranty != null ){
      MontoGarantia montoGarantia = articuloService.obtenerMontoGarantia( warranty.precio )
      if( montoGarantia != null && (montoGarantia.montoMinimo.compareTo(priceItem) <= 0
              && montoGarantia.montoMaximo.compareTo(priceItem) >= 0 ) ){
        warrantyAmount = montoGarantia.montoGarantia
      }
    }
    return warrantyAmount
  }



  static void printWarranty( BigDecimal amount, Integer idItem, String idOrder ){
    notaVentaService.guardaClaveSeguro( amount, idItem, idOrder )
    ticketService.imprimeGarantia( amount, idItem, idOrder )
  }

  static MontoGarantia findWarranty( BigDecimal warrantyAmount ){
    return articuloService.obtenerMontoGarantia( warrantyAmount )
  }

  static Boolean reprintWarranty( String ticket ){
    Boolean success = true
    try{
      Sucursal suc = RepositoryFactory.siteRepository.findOne( Registry.currentSite )
      NotaVenta nv = notaVentaService.obtenerNotaVentaPorTicket(StringUtils.trimToEmpty(StringUtils.trimToEmpty(suc.centroCostos)+"-"+ticket))
      if(nv != null && StringUtils.trimToEmpty(nv.udf4).length() > 0 ){
        String[] warranties = StringUtils.trimToEmpty(nv.udf4).split(/\|/)
        for(String warranty : warranties){
          String[] data = warranty.split(",")
          if( data.length >= 2 ){
            BigDecimal amount = BigDecimal.ZERO
            Integer idItem = 0
            try{
              idItem = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(data[1]))
            } catch (NumberFormatException e ){
              println e.message
              success = false
            }
            ticketService.imprimeGarantia( amount, idItem, StringUtils.trimToEmpty(nv.id) )
          }
        }
      } else {
        success = false
      }
    } catch ( Exception e ){
      println e.message
      success = false
    }
    return success
  }
}
