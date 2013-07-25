package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.repository.JbLlamadaRepository
import mx.lux.pos.repository.JbRepository
import mx.lux.pos.repository.JbTrackRepository
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.MainWindow
import mx.lux.pos.ui.resources.ServiceManager
import mx.lux.pos.ui.view.dialog.EntregaTrabajoDialog
import mx.lux.pos.ui.view.dialog.ManualPriceDialog
import mx.lux.pos.ui.view.panel.OrderPanel
import org.apache.commons.lang.NumberUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.swing.JOptionPane
import javax.swing.JPanel

import mx.lux.pos.model.*
import mx.lux.pos.service.*
import mx.lux.pos.ui.model.*

import java.text.SimpleDateFormat

@Slf4j
@Component
class OrderController {

  private static final Double ZERO_TOLERANCE = 0.0005

  private static NotaVentaService notaVentaService
  private static DetalleNotaVentaService detalleNotaVentaService
  private static PagoService pagoService
  private static TicketService ticketService
  private static BancoService bancoService
  private static InventarioService inventarioService
  private static MonedaExtranjeraService fxService
  private static Boolean displayUsd
  private static PromotionService promotionService
  private static CancelacionService cancelacionService
  private static RecetaService recetaService
  private static ArticuloService articuloService
  private static JbRepository jbRepository
  private static JbTrackRepository jbTrackRepository
  private static JbLlamadaRepository jbLlamadaRepository

  private static final String TAG_USD = "USD"

  @Autowired
  public OrderController(
      NotaVentaService notaVentaService,
      DetalleNotaVentaService detalleNotaVentaService,
      PagoService pagoService,
      TicketService ticketService,
      BancoService bancoService,
      InventarioService inventarioService,
      MonedaExtranjeraService monedaExtranjeraService,
      PromotionService promotionService,
      CancelacionService cancelacionService,
       RecetaService recetaService,
       ArticuloService articuloService,
       JbRepository jbRepository,
       JbTrackRepository jbTrackRepository,
       JbLlamadaRepository jbLlamadaRepository

  ) {
    this.notaVentaService = notaVentaService
    this.detalleNotaVentaService = detalleNotaVentaService
    this.pagoService = pagoService
    this.ticketService = ticketService
    this.bancoService = bancoService
    this.inventarioService = inventarioService
    fxService = monedaExtranjeraService
    this.promotionService = promotionService
      this.cancelacionService = cancelacionService
        this.recetaService = recetaService
      this.articuloService = articuloService
      this.jbRepository = jbRepository
      this.jbTrackRepository = jbTrackRepository
      this.jbLlamadaRepository = jbLlamadaRepository
  }

  static Order getOrder( String orderId ) {
    log.info( "obteniendo orden id: ${orderId}" )
    NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( orderId )
    Order order = Order.toOrder( notaVenta )
    if ( StringUtils.isNotBlank( order?.id ) ) {
      order.items?.clear()
      List<DetalleNotaVenta> detalles = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura( orderId )
      detalles?.each { DetalleNotaVenta tmp ->
        order.items?.add( OrderItem.toOrderItem( tmp ) )
      }
      order.payments?.clear()
      List<Pago> pagos = pagoService.listarPagosPorIdFactura( orderId )
      pagos?.each { Pago tmp ->
        Payment paymentTmp = Payment.toPaymment( tmp )
        if ( tmp?.idBancoEmisor?.integer ) {
          BancoEmisor banco = bancoService.obtenerBancoEmisor( tmp?.idBancoEmisor?.toInteger() )
          paymentTmp.issuerBank = banco?.descripcion
        }
        order.payments?.add( paymentTmp )
      }
      return order
    } else {
      log.warn( 'no se obtiene orden, notaVenta no existe' )
    }
    return null
  }

  static Order openOrder(String clienteID,String empID ) {
    log.info( 'abriendo nueva orden' )

    NotaVenta notaVenta = notaVentaService.abrirNotaVenta(clienteID,empID )
    return Order.toOrder( notaVenta )
  }

  static Item findArt(String dioptra){

           Articulo art = articuloService.findbyName(dioptra)

      return Item.toItem(art)
  }

  static Receta findRx(Order order,Customer customer){

      NotaVenta rxNotaVenta = notaVentaService.obtenerNotaVenta(order?.id)

      List<Rx> recetas = CustomerController.findAllPrescriptions(customer?.id)

      Receta receta = new Receta()

      Iterator iterator = recetas.iterator();
      while (iterator.hasNext()) {

          Rx rx = iterator.next()


          if(rxNotaVenta.receta == rx?.id){
              rxNotaVenta.receta
              receta = recetaService.findbyId(rxNotaVenta.receta)


          }


      }

      return receta
  }

    static void savePago(Pago pago){
        pagoService.actualizarPago(pago)
    }

    static Integer reciboSeq(){
      return pagoService.reciboSeq().toInteger()
    }

    static List<Pago> findPagos(String IdFactura){

       List<Pago> pagos =  pagoService.listarPagosPorIdFactura(IdFactura)

       return pagos
    }

    static void savePromisedDate(String idNotaVenta, Date fechaPrometida){
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(idNotaVenta)
        notaVentaService.saveProDate(notaVenta ,fechaPrometida)

    }

    static void saveRxOrder(String idNotaVenta, Integer receta){

       NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(idNotaVenta)


       notaVentaService.saveRx(notaVenta ,receta)

  }

  static void saveFrame (String idNotaVenta, String opciones, String forma){
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(idNotaVenta)
        notaVentaService.saveFrame(notaVenta,opciones,forma)
    }

  static Dioptra addDioptra(Order order, String dioptra){


      NotaVenta nota = notaVentaService.obtenerNotaVenta( order.id )
       nota.setCodigo_lente(dioptra)
      nota = notaVentaService.registrarNotaVenta( nota )


        Dioptra diop = generaDioptra(preDioptra(nota.codigo_lente))


      return diop

  }
    static String preDioptra(String dioString){
        String preDioptra
        //try{
        if (!dioString.equals(null)) {
        preDioptra = dioString.substring(0,1) + ',' +
                dioString.substring(1,2) + ',' +
                               dioString.substring(2,3) + ',' +
                   dioString.substring(3,5) + ',' +
                   dioString.substring(5,6) + ',' +
                   dioString.substring(6,7)
        }else{
            preDioptra = dioString
        }
        //}catch(e){}
        return preDioptra
    }

    static Dioptra generaDioptra(String dioString){


        Dioptra nuevoDioptra =  new Dioptra()
        if(dioString == null){

        }else{
            ArrayList<String> caract = new ArrayList<String>()
            String s = dioString
            StringTokenizer st = new StringTokenizer(s.trim(), ",")
            Iterator its = st.iterator()

            while (its.hasNext())
            {
                caract.add(its.next().toString())
            }
            nuevoDioptra =  new Dioptra(caract.get(0).toString(),caract.get(1).toString(),caract.get(2).toString(),caract.get(3).toString(),caract.get(4).toString(),caract.get(5).toString())
        }

        return nuevoDioptra

    }

  static Order addItemToOrder( Order order, Item item) {
      String orderId = order?.id
      String clienteID = order.customer?.id
      String empleadoID = order?.employee

      log.info( "agregando articulo id: ${item?.id} a orden id: ${orderId}" )
    if ( item?.id ) {
      orderId = ( notaVentaService.obtenerNotaVenta( orderId ) ? orderId : openOrder(clienteID,empleadoID)?.id )
      NotaVenta nota = notaVentaService.obtenerNotaVenta( orderId )
      DetalleNotaVenta detalle = null
      if ( item.isManualPriceItem() ) {
        String rmks = nota.observacionesNv
        ManualPriceDialog dlg = ManualPriceDialog.instance
        dlg.item = item
        dlg.remarks = rmks
        dlg.activate()
        if ( dlg.itemAccepted ) {
          item.listPrice = item.price
          detalle = new DetalleNotaVenta(
              idArticulo: item.id,
              cantidadFac: 1,
              precioUnitLista: item.listPrice,
              precioUnitFinal: item.price,
              precioCalcLista: item.listPrice,
              precioFactura: item.price,
              precioCalcOferta: 0,
              precioConv: 0,
              idTipoDetalle: 'N',
              surte: 'S'

          )
          nota.observacionesNv = dlg.remarks

          notaVentaService.registrarNotaVenta( nota )
        }
      } else {
        detalle = new DetalleNotaVenta(
            idArticulo: item.id,
            cantidadFac: 1,
            precioUnitLista: item.listPrice,
            precioUnitFinal: item.price,
            precioCalcLista: item.listPrice,
            precioFactura: item.price,
            precioCalcOferta: 0,
            precioConv: 0,
            idTipoDetalle: 'N',
            surte: 'S'

        )
      }
      if ( detalle != null ) {
        nota = notaVentaService.registrarDetalleNotaVentaEnNotaVenta( orderId, detalle )
      }
      return Order.toOrder( nota )
    } else {
      log.warn( "no se agrega articulo, parametros invalidos" )
    }
    return null
  }

  static Order addOrderItemToOrder( String orderId, OrderItem orderItem ) {
    log.info( "actualizando orderItem id: ${orderItem?.item?.id} en orden id: ${orderId}" )
    if ( StringUtils.isNotBlank( orderId ) && orderItem?.item?.id ) {
      DetalleNotaVenta detalle = new DetalleNotaVenta(
          idArticulo: orderItem.item.id,
          cantidadFac: orderItem.quantity ?: 1,
          precioUnitLista: orderItem.item.listPrice,
          precioUnitFinal: orderItem.item.price,
          precioCalcLista: orderItem.item.listPrice,
          precioFactura: orderItem.item.price,
          precioCalcOferta: 0,
          precioConv: 0,
          idTipoDetalle: 'N',
          surte: 'S'
      )
      NotaVenta notaVenta = notaVentaService.registrarDetalleNotaVentaEnNotaVenta( orderId, detalle )
      return Order.toOrder( notaVenta )
    } else {
      log.warn( "no se actualiza articulo, parametros invalidos" )
    }
    return null
  }


    static String codigoDioptra(Dioptra codDioptra) {

        String codigo
        if(!codDioptra.equals(null)){
         codigo = codDioptra.material + codDioptra.lente + codDioptra.tipo + codDioptra.especial + codDioptra.tratamiento + codDioptra.color
        }else{
           codigo = null
        }
            return codigo
    }

  static Order removeOrderItemFromOrder( String orderId, OrderItem orderItem ) {
    log.info( "eliminando orderItem, articulo id: ${orderItem?.item?.id} de orden id: ${orderId}" )
    if ( StringUtils.isNotBlank( orderId ) && orderItem?.item?.id ) {
      NotaVenta notaVenta = notaVentaService.eliminarDetalleNotaVentaEnNotaVenta( orderId, orderItem.item.id )
      if ( notaVenta?.id ) {
          NotaVenta nota = notaVentaService.obtenerNotaVenta(orderId)
          Order o = new Order()
          Articulo i = articuloService.obtenerArticulo(orderItem?.item?.id.toInteger())

          if(!i?.indice_dioptra.equals(null) ){
          Dioptra actDioptra = validaDioptra(generaDioptra(preDioptra(nota.codigo_lente)),generaDioptra(i.indice_dioptra))
          o = Order.toOrder( notaVenta )

          actDioptra = addDioptra(o,codigoDioptra(actDioptra))

          }

                      return o

      } else {
        log.warn( "no se elimina orderItem, notaVenta no existe" )
      }
    } else {
      log.warn( "no se elimina orderItem, parametros invalidos" )
    }
    return null
  }


    static Dioptra validaDioptra(Dioptra dioptra, Dioptra nuevoDioptra ){

        if(dioptra.getMaterial().toString().equals('@')||dioptra?.material == null||(dioptra.getMaterial().toString().equals('C') && !nuevoDioptra.getMaterial().toString().equals('@'))||(!dioptra.getMaterial().toString().trim().equals('C')&&!nuevoDioptra.getMaterial().toString().trim().equals('@'))){
            if(dioptra.getMaterial().toString().trim().equals( nuevoDioptra.getMaterial().toString().trim())){
             dioptra.setMaterial('C')
            }else{
            dioptra.setMaterial(nuevoDioptra.getMaterial())
            }
        }
        if(dioptra.getLente().toString().equals('@')||dioptra?.lente == null||!nuevoDioptra?.getLente().toString().equals('@') ){
            if(dioptra.getLente().toString().trim().equals( nuevoDioptra.getLente().toString().trim())){
                dioptra.setLente('@')
            }else{
            dioptra.setLente(nuevoDioptra.getLente())
            }
        }
        if(dioptra.getTipo().toString().equals('@')||dioptra?.tipo == null||(dioptra.getTipo().toString().equals('N')&&!nuevoDioptra.getTipo().toString().equals('@'))||(!dioptra.getTipo().toString().trim().equals('N')&&!nuevoDioptra.getTipo().toString().trim().equals('@'))){
            if(dioptra.getTipo().toString().trim().equals( nuevoDioptra.getTipo().toString().trim())){
                dioptra.setTipo('N')
            }else{
            dioptra.setTipo(nuevoDioptra.getTipo())
            }
        }
        if(dioptra.getEspecial().toString().equals('@@')||dioptra?.especial == null||(dioptra.getEspecial().toString().equals('BL')&&!nuevoDioptra.getEspecial().toString().equals('@@'))||(!dioptra.getEspecial().toString().trim().equals('BL')&&!nuevoDioptra.getEspecial().toString().trim().equals('@@'))){
            if(dioptra.getEspecial().toString().trim().equals( nuevoDioptra.getEspecial().toString().trim())){
                dioptra.setEspecial('BL')
            }else{
            dioptra.setEspecial(nuevoDioptra.getEspecial())
            }
        }
        if(dioptra.getTratamiento().toString().equals('@')||dioptra?.tratamiento == null||(dioptra.getTratamiento().toString().equals('B')&&!nuevoDioptra.getTratamiento().toString().equals('@'))||(!dioptra.getTratamiento().toString().trim().equals('B')&&!nuevoDioptra.getTratamiento().toString().trim().equals('@'))){
            if(dioptra.getTratamiento().toString().trim().equals( nuevoDioptra.getTratamiento().toString().trim())){
                dioptra.setTratamiento('B')
            }else{
            dioptra.setTratamiento(nuevoDioptra.getTratamiento())
            }
        }
        if(dioptra.getColor().toString().trim().equals('@')||dioptra?.color == null||(dioptra.getColor().toString().trim().equals('B')&&!nuevoDioptra.getColor().toString().trim().equals('@'))||(!dioptra.getColor().toString().trim().equals('B')&&!nuevoDioptra.getColor().toString().trim().equals('@'))){

            if(dioptra.getColor().toString().trim().equals( nuevoDioptra.getColor().toString().trim())){
                dioptra.setColor('B')
            }else{
            dioptra.setColor(nuevoDioptra.getColor())
            }
        }
        return dioptra
    }


    static Pago addPaymentToOrder( String orderId, Payment payment ) {
    log.info( "agregando pago monto: ${payment?.amount}, tipo: ${payment?.paymentTypeId} a orden id: ${orderId}" )
    if ( StringUtils.isNotBlank( orderId ) && StringUtils.isNotBlank( payment?.paymentTypeId ) && payment?.amount ) {

      User user = Session.get( SessionItem.USER ) as User

      Pago pago = new Pago(
          idFormaPago: payment.paymentTypeId,
          referenciaPago: payment.paymentReference,
          monto: payment.amount,
          idEmpleado: user?.username,
          idFPago: payment.paymentTypeId,
          clave: payment.paymentReference,
          referenciaClave: payment.codeReference,
          idBancoEmisor: payment.issuerBankId,
          idTerminal: payment.terminalId,
          idPlan: payment.planId
      )
      Pago newPago = notaVentaService.registrarPagoEnNotaVenta( orderId, pago )
      return newPago
    } else {
      log.warn( "no se agrega pago, parametros invalidos" )
    }
    return null
  }

  static Order removePaymentFromOrder( String orderId, Payment payment ) {
    log.info( "eliminando pago id: ${payment?.id}, monto: ${payment?.amount}, tipo: ${payment?.paymentTypeId}" )
    log.info( "de orden id: ${orderId}" )
    if ( StringUtils.isNotBlank( orderId ) && payment?.id ) {
        cancelacionService.restablecerMontoAlBorrarPago( payment.id )
        NotaVenta notaVenta = notaVentaService.eliminarPagoEnNotaVenta( orderId, payment.id )
      if ( notaVenta?.id ) {
        return Order.toOrder( notaVenta )
      } else {
        log.warn( "no se elimina pago, notaVenta no existe" )
      }
    } else {
      log.warn( "no se elimina pago, parametros invalidos" )
    }
    return null
  }

  static Order placeOrder( Order order ) {
    log.info( "registrando orden id: ${order?.id}, cliente: ${order?.customer?.id}" )
    if ( StringUtils.isNotBlank( order?.id ) && order?.customer?.id ) {
      NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( order.id )
      if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
        User user = Session.get( SessionItem.USER ) as User
        if ( StringUtils.isBlank( notaVenta.idEmpleado ) ) {
          notaVenta.idEmpleado = user?.username
        }
        if ( notaVenta.idCliente != null ) {
          notaVenta.idCliente = order.customer.id
        }
        notaVenta.observacionesNv = order.comments
        notaVenta.empEntrego = user?.username
        notaVenta.udf2 = order.country.toUpperCase()
        notaVenta = notaVentaService.cerrarNotaVenta( notaVenta )
        if ( inventarioService.solicitarTransaccionVenta( notaVenta ) ) {
          log.debug( "transaccion de inventario correcta" )
        } else {
          log.warn( "no se pudo procesar la transaccion de inventario" )
        }
        ServiceManager.ioServices.logSalesNotification( notaVenta.id )
        return Order.toOrder( notaVenta )
      } else {
        log.warn( "no se registra orden, notaVenta no existe" )
      }
    } else {
      log.warn( "no se registra orden, parametros invalidos" )
    }
    return null
  }

  static void fieldRX(String orderId){
      if (StringUtils.isNotBlank( orderId ) ) {
          recetaService.generaAcuse(orderId)
      } else {
          log.warn( "no se imprime receta, parametros invalidos" )
      }

  }

  static void printPaid(String orderId, Integer pagoId){
      if (StringUtils.isNotBlank( orderId) ) {
          ticketService.imprimePago(orderId, pagoId )
      } else {
          log.warn( "no se imprime pago, parametros invalidos" )
      }
  }

  static void printRx(String orderId){
      log.info( "imprimiendo receta id: " )
      if (StringUtils.isNotBlank( orderId ) ) {
          ticketService.imprimeRx(orderId)
      } else {
          log.warn( "no se imprime receta, parametros invalidos" )
      }
  }

    static Jb entraJb(String rx){
        return  jbRepository.findOne(rx)
    }

    static void insertaEntrega(Order order, Jb trabajo){
        NotaVenta notaVenta =  notaVentaService.obtenerNotaVenta(order?.id)
        User user = Session.get( SessionItem.USER ) as User
        notaVenta.setEmpEntrego(user?.username)


        notaVenta.setHoraEntrega(new Date())
        notaVenta.setFechaEntrega(new Date())

        notaVentaService.saveOrder(notaVenta)

        trabajo.setEstado('TE')



       trabajo = jbRepository.saveAndFlush(trabajo)

        JbTrack jbTrack =  new JbTrack()
        jbTrack?.rx = order?.bill
        jbTrack?.estado = 'TE'
        jbTrack?.emp =  user?.username
        jbTrack?.fecha = new Date()
         jbTrack?.id_mod = '0'
         jbTrack?.id_viaje = null
         jbTrack?.obs = user?.username


        jbTrackRepository.saveAndFlush(jbTrack)

        jbLlamadaRepository.deleteByJbLlamada(order?.bill)

        if(trabajo?.id_grupo != null){
             //Hoja de Proceso y Casos de Uso Pagar Saldo y Entregar - p_pagar_saldos, al entregar la venta. iv)
        }

    }

  static void printOrder( String orderId ) {
    printOrder( orderId, true )
  }

  static void printOrder( String orderId, boolean pNewOrder ) {
    log.info( "imprimiendo orden id: ${orderId}" )
    if ( StringUtils.isNotBlank( orderId ) ) {
      ticketService.imprimeVenta( orderId, pNewOrder )
    } else {
      log.warn( "no se imprime orden, parametros invalidos" )
    }
  }

  static List<Order> findLastOrders( ) {
    log.info( "obteniendo ultimas ordenes" )
    List<NotaVenta> results = notaVentaService.listarUltimasNotasVenta()
    return results?.collect { NotaVenta tmp ->
      Order.toOrder( tmp )
    }
  }

  static List<Order> findOrdersByParameters( Map<String, Object> params ) {
    log.info( "buscando ordenes por parametros: ${params}" )
    List<NotaVenta> results = notaVentaService.listarNotasVentaPorParametros( params )
    log.debug( "ordenes obtenidas: ${results*.id}" )
    return results.collect { NotaVenta tmp ->
      Order.toOrder( tmp )
    }
  }

  static Order findOrderByTicket( String ticket ) {
    log.info( "buscando orden por ticket: ${ticket}" )
    NotaVenta result = notaVentaService.obtenerNotaVentaPorTicket( ticket )
    return Order.toOrder( result )
  }

  static Double requestUsdRate( ) {
    log.info( "Request USD rate" )

    Double rate = 1.0
    MonedaDetalle fxrate = fxService.findActiveRate( TAG_USD )

    if ( fxrate != null ) {
      rate = fxrate.tipoCambio.doubleValue()

    }
    return rate
  }

  static Boolean requestUsdDisplayed( ) {
    if ( displayUsd == null ) {
      log.info( "Request USD rate" )

      displayUsd = fxService.requestUsdDisplayed()
    }
    return displayUsd
  }

  static SalesWithNoInventory requestConfigSalesWithNoInventory( ) {
    return notaVentaService.obtenerConfigParaVentasSinInventario()
  }

  static DetalleNotaVenta getDetalleNotaVenta( String idFactura, Integer idArticulo ) {
    log.debug( "getDetalleNotaVenta( String idFactura, Integer idArticulo )" )

    DetalleNotaVenta venta = detalleNotaVentaService.obtenerDetalleNotaVenta( idFactura, idArticulo )

    return venta
  }

  static Promocion getPromocion( Integer idPromocion ) {
    log.debug( "getPromocion( Integer idPromocion )" )
    Promocion promocion = promotionService.obtenerPromocion( idPromocion )
    return promocion
  }

  static void requestSaveAsQuote( Order pOrder, Customer pCustomer ) {

    Integer pQuoteId = ServiceManager.quote.copyFromOrder( pOrder.id, pCustomer.id,
        ( ( User ) Session.get( SessionItem.USER ) ).username )

    if ( pQuoteId != null ) {
      ticketService.imprimeCotizacion( pQuoteId )
      notaVentaService.eliminarNotaVenta( pOrder.id )
      String msg = String.format( 'La cotización fue registrada como: %d    ', pQuoteId )
      JOptionPane.showMessageDialog( MainWindow.instance, msg, 'Cotización', JOptionPane.INFORMATION_MESSAGE )
    }
  }

  static String requestOrderFromQuote( JPanel pComponent ) {
    String orderNbr = null
    String confirm = JOptionPane.showInputDialog( MainWindow.instance, OrderPanel.MSG_INPUT_QUOTE_ID,
        OrderPanel.TXT_QUOTE_TITLE, JOptionPane.QUESTION_MESSAGE )
    if ( StringUtils.trimToNull( confirm ) != null ) {
      Integer quoteNbr = NumberUtils.createInteger( StringUtils.trimToEmpty( confirm ) )
      if ( quoteNbr != null ) {
        Map<String, Object> result = ServiceManager.quote.toOrder( quoteNbr )
        if ( result != null ) {
          orderNbr = StringUtils.trimToNull( ( String ) result.get( 'orderNbr' ) )
        }
        if ( orderNbr == null ) {
          JOptionPane.showMessageDialog( MainWindow.instance, ( String ) result.get( 'statusMessage' ),
              OrderPanel.TXT_QUOTE_TITLE, JOptionPane.ERROR_MESSAGE )
        }
      }
    }
    return orderNbr
  }

  static String requestEmployee( String pOrderId ) {

      String empName = ''
    if ( StringUtils.trimToNull( StringUtils.trimToEmpty(pOrderId) ) != null ) {
      Empleado employee = notaVentaService.obtenerEmpleadoDeNotaVenta( pOrderId )
      if ( employee != null ) {
        if ( ( ( User ) Session.get( SessionItem.USER ) ).equals( employee ) ) {
          empName = ( ( User ) Session.get( SessionItem.USER ) ).toString()
        } else {
          empName = User.toUser( employee ).toString()
        }
      }
    }
    return empName
  }

  static void saveCustomerForOrder( String pOrderNbr, Integer pCustomerId ) {
    if ( StringUtils.isNotBlank( pOrderNbr ) ) {
      NotaVenta order = notaVentaService.obtenerNotaVenta( pOrderNbr )
      if ( order != null ) {
        order.idCliente = pCustomerId
        notaVentaService.saveOrder( order )
      }
    }
  }

  static Customer getCustomerFromOrder( String pOrderNbr ) {
    Customer cust = null
    if ( StringUtils.trimToNull( pOrderNbr ) != null ) {
      NotaVenta order = notaVentaService.obtenerNotaVenta( pOrderNbr )
      if ( order != null ) {
        cust = Customer.toCustomer( order.cliente )
      }
    }
    return cust
  }

  static Order saveOrder( Order order ) {
    log.info( "registrando orden id: ${order?.id}, cliente: ${order?.customer?.id}" )
    if ( StringUtils.isNotBlank( order?.id ) && order?.customer?.id ) {
      NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( order.id )
      if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
        User user = Session.get( SessionItem.USER ) as User
        if ( StringUtils.isBlank( notaVenta.idEmpleado ) ) {
          notaVenta.idEmpleado = user?.username
        }
        if ( notaVenta.idCliente != null ) {
          notaVenta.idCliente = order.customer.id
        }
        notaVenta.codigo_lente = order?.dioptra
        notaVenta.observacionesNv = order.comments
        notaVenta = notaVentaService.registrarNotaVenta( notaVenta )
        return Order.toOrder( notaVenta )
      } else {
        log.warn( "no se registra orden, notaVenta no existe" )
      }
    } else {
      log.warn( "no se registra orden, parametros invalidos" )
    }
    return null
  }

  static void notifyAlert( String pTitle, String pMessage ) {
    JOptionPane.showMessageDialog( MainWindow.instance, pMessage, pTitle, JOptionPane.ERROR_MESSAGE )
  }

  static Boolean isPaymentPolicyFulfilled( Order pOrder ) {


    Boolean result = true
    if ( pOrder.due < 0 ) {
      this.notifyAlert( OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Los pagos no deben ser mayores al total de la venta.' )
      result = false
    } else if ( pOrder.containsOphtalmic() ) {


                 /*
      if ( pOrder.advancePct < ( SettingsController.instance.advancePct - ZERO_TOLERANCE ) ) {
        this.notifyAlert( OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Pago menor al %Anticipo establecido.' )
        result = false
      }
               */
    } else if ( pOrder.due > 0 ) {
      this.notifyAlert( OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Se debe cubrir el total del saldo.' )
      result = false
    }
    return result
  }

  static void requestNextOrderFromCustomer( Customer pCustomer, CustomerListener pListener ) {
    NotaVenta dbOrder = notaVentaService.obtenerSiguienteNotaVentaDeCliente( pCustomer.id )
    if ( dbOrder != null ) {
      Order o = Order.toOrder( dbOrder )
      pListener.disableUI()
      pListener.operationTypeSelected = OperationType.PAYING
      pListener.setCustomer( pCustomer )
      pListener.setOrder( o )
      pListener.enableUI()
    }
  }


  static void validaEntrega( String idFactura){

     NotaVenta notaVenta =  notaVentaService.obtenerNotaVentaPorTicket(idFactura)
     Order order = Order.toOrder(notaVenta)
     List<DetalleNotaVenta> detalleVenta = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(notaVenta?.id)
     String surte
      Iterator iterator = detalleVenta.iterator();
      while (iterator.hasNext()) {

          DetalleNotaVenta detalle = iterator.next()

          if(detalle?.surte != null){
              surte = detalle?.surte
          }
      }
         println('generico: ' + notaVenta?.codigo_lente )
      println('surte: ' + surte)
      if(notaVenta?.codigo_lente == null && !surte.equals('P')){
        String genericoPB = notaVenta?.codigo_lente.trim().substring(1,2)

            SimpleDateFormat fecha = new SimpleDateFormat("dd/MMMM/yyyy")
            String fechaVenta = fecha.format(notaVenta?.fechaHoraFactura)
            String ahora = fecha.format(new Date())
            println('Fecha venta: ' + fechaVenta)

            if(!fechaVenta.equals(ahora)){
                println('Fecha venta es diferente de ahora')



                if((order?.total - order?.paid) == 0){


                    Jb trabajo = OrderController.entraJb(order?.bill)
                    if(trabajo != null){


                        if(trabajo?.estado.trim().equals('RS')){

                                OrderController.insertaEntrega(order,trabajo)
                                //insercion despues de entregar
                                JOptionPane.showMessageDialog(null,"datos guardados correctamente")

                        }else{
                            JOptionPane.showMessageDialog(null,"Estado no es igual a RS")
                        }

                    }else{
                        JOptionPane.showMessageDialog(null,"No hay registro en Jb")
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Existe adeudo")
                }



            } else{
                JOptionPane.showMessageDialog(null,"fecha igual a ahora")
            }





    }else{
        JOptionPane.showMessageDialog(null,"Generico B o surte P")
    }
}

}
