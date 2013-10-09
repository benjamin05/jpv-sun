package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.repository.AcuseRepository
import mx.lux.pos.repository.AcusesTipoRepository
import mx.lux.pos.repository.DescuentoClaveRepository
import mx.lux.pos.repository.GenericoRepository
import mx.lux.pos.repository.JbLlamadaRepository
import mx.lux.pos.repository.JbNotasRepository
import mx.lux.pos.repository.JbRepository
import mx.lux.pos.repository.JbServiciosRepository
import mx.lux.pos.repository.ParametroRepository
import mx.lux.pos.repository.PrecioRepository
import mx.lux.pos.repository.TmpServiciosRepository
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.MainWindow
import mx.lux.pos.ui.resources.ServiceManager
import mx.lux.pos.ui.view.dialog.ContactClientDialog
import mx.lux.pos.ui.view.dialog.ContactDialog

import mx.lux.pos.ui.view.dialog.ManualPriceDialog
import mx.lux.pos.ui.view.panel.OrderPanel
import org.apache.commons.lang.NumberUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.JPanel

import mx.lux.pos.model.*
import mx.lux.pos.service.*
import mx.lux.pos.ui.model.*

import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException



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
    private static JbTrackService jbTrackService
    private static JbLlamadaRepository jbLlamadaRepository
    private static ParametroRepository parametroRepository
    private static TmpServiciosRepository tmpServiciosRepository
    private static DescuentoClaveRepository descuentoClaveRepository
    private static GenericoRepository genericoRepository
    private static PrecioRepository precioRepository
    private static AcusesTipoRepository acusesTipoRepository
    private static AcuseRepository acuseRepository
    private static JbServiciosRepository jbServiciosRepository
    private static JbNotasRepository jbNotasRepository

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
            JbTrackService jbTrackService,
            JbLlamadaRepository jbLlamadaRepository,
            ParametroRepository parametroRepository,
            TmpServiciosRepository tmpServiciosRepository,
            DescuentoClaveRepository descuentoClaveRepository,
            GenericoRepository genericoRepository,
            PrecioRepository precioRepository,
            AcusesTipoRepository acusesTipoRepository,
            AcuseRepository acuseRepository,
            JbServiciosRepository jbServiciosRepository,
            JbNotasRepository jbNotasRepository

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
        this.jbTrackService = jbTrackService
        this.jbLlamadaRepository = jbLlamadaRepository
        this.parametroRepository = parametroRepository
        this.tmpServiciosRepository = tmpServiciosRepository
        this.descuentoClaveRepository = descuentoClaveRepository
        this.genericoRepository = genericoRepository
        this.precioRepository = precioRepository
        this.acusesTipoRepository = acusesTipoRepository
        this.acuseRepository = acuseRepository
        this.jbServiciosRepository = jbServiciosRepository
        this.jbNotasRepository = jbNotasRepository
    }

    static Order getOrder(String orderId) {
        log.info("obteniendo orden id: ${orderId}")
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(orderId)
        Order order = Order.toOrder(notaVenta)
        if (StringUtils.isNotBlank(order?.id)) {
            order.items?.clear()
            List<DetalleNotaVenta> detalles = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(orderId)
            detalles?.each { DetalleNotaVenta tmp ->
                order.items?.add(OrderItem.toOrderItem(tmp))
            }
            order.payments?.clear()
            List<Pago> pagos = pagoService.listarPagosPorIdFactura(orderId)
            pagos?.each { Pago tmp ->
                Payment paymentTmp = Payment.toPaymment(tmp)
                if (tmp?.idBancoEmisor?.integer) {
                    BancoEmisor banco = bancoService.obtenerBancoEmisor(tmp?.idBancoEmisor?.toInteger())
                    paymentTmp.issuerBank = banco?.descripcion
                }
                order.payments?.add(paymentTmp)
            }
            return order
        } else {
            log.warn('no se obtiene orden, notaVenta no existe')
        }
        return null
    }

    static Order openOrder(String clienteID, String empID) {
        log.info('abriendo nueva orden')

        NotaVenta notaVenta = notaVentaService.abrirNotaVenta(clienteID, empID)
        return Order.toOrder(notaVenta)
    }

    static Item findArt(String dioptra) {

        Articulo art = articuloService.findbyName(dioptra)

        return Item.toItem(art)
    }

    static Receta findRx(Order order, Customer customer) {

        NotaVenta rxNotaVenta = notaVentaService.obtenerNotaVenta(order?.id)

        List<Rx> recetas = CustomerController.findAllPrescriptions(customer?.id)

        Receta receta = new Receta()

        Iterator iterator = recetas.iterator();
        while (iterator.hasNext()) {

            Rx rx = iterator.next()


            if (rxNotaVenta.receta == rx?.id) {
                rxNotaVenta.receta
                receta = recetaService.findbyId(rxNotaVenta.receta)


            }


        }

        return receta
    }

    static void savePago(Pago pago) {
        pagoService.actualizarPago(pago)
    }

    static Integer reciboSeq() {
        return pagoService.reciboSeq().toInteger()
    }

    static List<Pago> findPagos(String IdFactura) {

        List<Pago> pagos = pagoService.listarPagosPorIdFactura(IdFactura)

        return pagos
    }

    static void savePromisedDate(String idNotaVenta, Date fechaPrometida) {
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(idNotaVenta)
        notaVentaService.saveProDate(notaVenta, fechaPrometida)

    }

    static void saveRxOrder(String idNotaVenta, Integer receta) {
        log.debug( "guardando receta ${receta}" )
        println 'receta con error'+receta
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(idNotaVenta)
        notaVentaService.saveRx(notaVenta, receta)
    }

    static Order saveFrame(String idNotaVenta, String opciones, String forma) {

        NotaVenta notaVenta = notaVentaService.saveFrame(idNotaVenta, opciones, forma)

        return Order.toOrder(notaVenta)
    }

    static Dioptra addDioptra(Order order, String dioptra) {
        NotaVenta nota = notaVentaService.obtenerNotaVenta(order.id)
        nota.setCodigo_lente(dioptra)
        nota = notaVentaService.registrarNotaVenta(nota)
        Dioptra diop = generaDioptra(preDioptra(nota.codigo_lente))
        println('Codigo Lente: ' + nota.codigo_lente)
        return diop
    }

    static String preDioptra(String dioString) {
        String preDioptra
        //try{
        if (!dioString.equals(null)) {
            preDioptra = dioString.substring(0, 1) + ',' +
                    dioString.substring(1, 2) + ',' +
                    dioString.substring(2, 3) + ',' +
                    dioString.substring(3, 5) + ',' +
                    dioString.substring(5, 6) + ',' +
                    dioString.substring(6, 7)
        } else {
            preDioptra = dioString
        }
        //}catch(e){}
        return preDioptra
    }

    static Dioptra generaDioptra(String dioString) {
        Dioptra nuevoDioptra = new Dioptra()
        if (dioString == null) {
        } else {
            ArrayList<String> caract = new ArrayList<String>()
            String s = dioString
            StringTokenizer st = new StringTokenizer(s.trim(), ",")
            Iterator its = st.iterator()
            while (its.hasNext()) {
                caract.add(its.next().toString())
            }
            nuevoDioptra = new Dioptra(caract.get(0).toString(), caract.get(1).toString(), caract.get(2).toString(), caract.get(3).toString(), caract.get(4).toString(), caract.get(5).toString())
        }
        return nuevoDioptra
    }

    static Order addItemToOrder(Order order, Item item, String surte) {
        String orderId = order?.id
        String clienteID = order.customer?.id
        String empleadoID = order?.employee

        log.info("agregando articulo id: ${item?.id} a orden id: ${orderId}")
        if (item?.id) {
            orderId = (notaVentaService.obtenerNotaVenta(orderId) ? orderId : openOrder(clienteID, empleadoID)?.id)
            NotaVenta nota = notaVentaService.obtenerNotaVenta(orderId)
            DetalleNotaVenta detalle = null
            if (item.isManualPriceItem()) {
                String rmks = nota.observacionesNv
                ManualPriceDialog dlg = ManualPriceDialog.instance
                dlg.item = item
                dlg.remarks = rmks
                dlg.activate()
                if (dlg.itemAccepted) {
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
                            surte: item?.type.trim().equalsIgnoreCase('B') ? 'P' : surte,
                    )
                    nota.observacionesNv = dlg.remarks

                    notaVentaService.registrarNotaVenta(nota)
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
                        surte: item?.type.trim().equalsIgnoreCase('B') ? 'P' : surte

                )
            }
            if (detalle != null) {
                nota = notaVentaService.registrarDetalleNotaVentaEnNotaVenta(orderId, detalle)
            }
            if (nota != null ) {
                notaVentaService.registraImpuestoPorFactura( nota )
            }
            return Order.toOrder(nota)
        } else {
            log.warn("no se agrega articulo, parametros invalidos")
        }
        return null
    }

    static Order addOrderItemToOrder(String orderId, OrderItem orderItem, String surte) {
        log.info("actualizando orderItem id: ${orderItem?.item?.id} en orden id: ${orderId}")
        if (StringUtils.isNotBlank(orderId) && orderItem?.item?.id) {
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
                    surte: surte
            )
            NotaVenta notaVenta = notaVentaService.registrarDetalleNotaVentaEnNotaVenta(orderId, detalle)
            return Order.toOrder(notaVenta)
        } else {
            log.warn("no se actualiza articulo, parametros invalidos")
        }
        return null
    }


    static String codigoDioptra(Dioptra codDioptra) {
        String codigo
        if (!codDioptra.equals(null)) {
            codigo = codDioptra.material + codDioptra.lente + codDioptra.tipo + codDioptra.especial + codDioptra.tratamiento + codDioptra.color
        } else {
            codigo = null
        }
        return codigo
    }

    static Order removeOrderItemFromOrder(String orderId, OrderItem orderItem) {
        log.info("eliminando orderItem, articulo id: ${orderItem?.item?.id} de orden id: ${orderId}")
        if (StringUtils.isNotBlank(orderId) && orderItem?.item?.id) {
            NotaVenta notaVenta = notaVentaService.eliminarDetalleNotaVentaEnNotaVenta(orderId, orderItem.item.id)
            if (notaVenta?.id) {
                NotaVenta nota = notaVentaService.obtenerNotaVenta(orderId)
                Order o = new Order()
                Articulo i = articuloService.obtenerArticulo(orderItem?.item?.id.toInteger())

                if (!i?.indice_dioptra.equals(null)) {
                    Dioptra actDioptra = validaDioptra(generaDioptra(preDioptra(nota.codigo_lente)), generaDioptra(i.indice_dioptra))
                    o = Order.toOrder(notaVenta)

                    actDioptra = addDioptra(o, codigoDioptra(actDioptra))

                }

                return o

            } else {
                log.warn("no se elimina orderItem, notaVenta no existe")
            }
        } else {
            log.warn("no se elimina orderItem, parametros invalidos")
        }
        return null
    }


    static Dioptra validaDioptra(Dioptra dioptra, Dioptra nuevoDioptra) {

        if (dioptra.getMaterial().toString().equals('@') || dioptra?.material == null || (dioptra.getMaterial().toString().equals('C') && !nuevoDioptra.getMaterial().toString().equals('@')) /* || (!dioptra.getMaterial().toString().trim().equals('C') && !nuevoDioptra.getMaterial().toString().trim().equals('@')) */ ) {
            if (dioptra.getMaterial().toString().trim().equals(nuevoDioptra.getMaterial().toString().trim())) {
                dioptra.setMaterial('C')
            } else {
                dioptra.setMaterial(nuevoDioptra.getMaterial())
            }
        }
        if (dioptra.getLente().toString().equals('@') || dioptra?.lente == null || !nuevoDioptra?.getLente().toString().equals('@')) {
            if (dioptra.getLente().toString().trim().equals(nuevoDioptra.getLente().toString().trim())) {
                dioptra.setLente('@')
            } else {
                dioptra.setLente(nuevoDioptra.getLente())
            }
        }
        if (dioptra.getTipo().toString().equals('@') || dioptra?.tipo == null || (dioptra.getTipo().toString().equals('N') && !nuevoDioptra.getTipo().toString().equals('@'))/* || (!dioptra.getTipo().toString().trim().equals('N') && !nuevoDioptra.getTipo().toString().trim().equals('@'))*/) {
            if (dioptra.getTipo().toString().trim().equals(nuevoDioptra.getTipo().toString().trim())) {
                dioptra.setTipo('N')
            } else {
                dioptra.setTipo(nuevoDioptra.getTipo())
            }
        }
        if (dioptra.getEspecial().toString().equals('@@') || dioptra?.especial == null || (dioptra.getEspecial().toString().equals('BL') && !nuevoDioptra.getEspecial().toString().equals('@@')) /* || (!dioptra.getEspecial().toString().trim().equals('BL') && !nuevoDioptra.getEspecial().toString().trim().equals('@@'))*/) {
            if (dioptra.getEspecial().toString().trim().equals(nuevoDioptra.getEspecial().toString().trim())) {
                dioptra.setEspecial('BL')
            } else {
                dioptra.setEspecial(nuevoDioptra.getEspecial())
            }
        }
        if (dioptra.getTratamiento().toString().equals('@') || dioptra?.tratamiento == null || (dioptra.getTratamiento().toString().equals('B') && !nuevoDioptra.getTratamiento().toString().equals('@')) /* || (!dioptra.getTratamiento().toString().trim().equals('B') && !nuevoDioptra.getTratamiento().toString().trim().equals('@'))*/) {
            if (dioptra.getTratamiento().toString().trim().equals(nuevoDioptra.getTratamiento().toString().trim())) {
                dioptra.setTratamiento('B')
            } else {
                dioptra.setTratamiento(nuevoDioptra.getTratamiento())
            }
        }
        if (dioptra.getColor().toString().trim().equals('@') || dioptra?.color == null || (dioptra.getColor().toString().trim().equals('B') && !nuevoDioptra.getColor().toString().trim().equals('@')) /* || (!dioptra.getColor().toString().trim().equals('B') && !nuevoDioptra.getColor().toString().trim().equals('@'))*/) {

            if (dioptra.getColor().toString().trim().equals(nuevoDioptra.getColor().toString().trim())) {
                dioptra.setColor('B')
            } else {
                dioptra.setColor(nuevoDioptra.getColor())
            }
        }
        return dioptra
    }


    static Pago addPaymentToOrder(String orderId, Payment payment) {
        log.info("agregando pago monto: ${payment?.amount}, tipo: ${payment?.paymentTypeId} a orden id: ${orderId}")
        if (StringUtils.isNotBlank(orderId) && StringUtils.isNotBlank(payment?.paymentTypeId) && payment?.amount) {

            User user = Session.get(SessionItem.USER) as User

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
            Pago newPago = notaVentaService.registrarPagoEnNotaVenta(orderId, pago)
            return newPago
        } else {
            log.warn("no se agrega pago, parametros invalidos")
        }
        return null
    }

    static Order removePaymentFromOrder(String orderId, Payment payment) {
        log.info("eliminando pago id: ${payment?.id}, monto: ${payment?.amount}, tipo: ${payment?.paymentTypeId}")
        log.info("de orden id: ${orderId}")
        if (StringUtils.isNotBlank(orderId) && payment?.id) {
            cancelacionService.restablecerMontoAlBorrarPago(payment.id)
            NotaVenta notaVenta = notaVentaService.eliminarPagoEnNotaVenta(orderId, payment.id)
            if (notaVenta?.id) {
                return Order.toOrder(notaVenta)
            } else {
                log.warn("no se elimina pago, notaVenta no existe")
            }
        } else {
            log.warn("no se elimina pago, parametros invalidos")
        }
        return null
    }



    static void entregaInstante(Order order) {
        log.info("registrando orden id: ${order?.id}, cliente: ${order?.customer?.id}")
        if (StringUtils.isNotBlank(order?.id) && order?.customer?.id) {
            NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order.id)
            if (StringUtils.isNotBlank(notaVenta?.id)) {
                User user = Session.get(SessionItem.USER) as User
                notaVenta?.empEntrego = user?.username
                notaVenta?.fechaEntrega = new Date()
                notaVenta?.horaEntrega = new Date()

                notaVentaService.saveOrder(notaVenta)
            }
        }
    }

    static Order placeOrder(Order order, String idEmpleado) {
        log.info("registrando orden id: ${order?.id}, cliente: ${order?.customer?.id}")
        if (StringUtils.isNotBlank(order?.id) && order?.customer?.id) {
            NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order.id)
            if (StringUtils.isNotBlank(notaVenta?.id)) {

              //  if (StringUtils.isBlank(notaVenta.idEmpleado)) {

                    notaVenta.idEmpleado = idEmpleado
              //  }
                if (notaVenta.idCliente != null) {
                    notaVenta.idCliente = order.customer.id
                }
                notaVenta.observacionesNv = order.comments
                //notaVenta.empEntrego = user?.username
                //notaVenta.udf2 = order.country.toUpperCase()
                notaVenta = notaVentaService.cerrarNotaVenta(notaVenta)
                if (inventarioService.solicitarTransaccionVenta(notaVenta)) {
                    log.debug("transaccion de inventario correcta")
                } else {
                    log.warn("no se pudo procesar la transaccion de inventario")
                }
                ServiceManager.ioServices.logSalesNotification(notaVenta.id)
                return Order.toOrder(notaVenta)
            } else {
                log.warn("no se registra orden, notaVenta no existe")
            }
        } else {
            log.warn("no se registra orden, parametros invalidos")
        }
        return null
    }

    static void fieldRX(String orderId) {
        if (StringUtils.isNotBlank(orderId)) {
            recetaService.generaAcuse(orderId)
        } else {
            log.warn("no se imprime receta, parametros invalidos")
        }

    }

    static void printPaid(String orderId, Integer pagoId) {
        if (StringUtils.isNotBlank(orderId)) {
            ticketService.imprimePago(orderId, pagoId)
        } else {
            log.warn("no se imprime pago, parametros invalidos")
        }
    }

    static Order notaVentaxRx(Integer rx){
        return Order.toOrder(notaVentaService.notaVentaxRx(rx))
    }

    static void printRx(String orderId, Boolean reimpresion) {
        log.info("imprimiendo receta id: ")
        if (StringUtils.isNotBlank(orderId)) {
            ticketService.imprimeRx(orderId, reimpresion)
        } else {
            log.warn("no se imprime receta, parametros invalidos")

        }
    }


    static Jb entraJb(String rx) {
        return jbRepository.findOne(rx)
    }

    static void insertaEntrega(Order order, Boolean entregaInstante) {
        println('Order ID: ' + order?.id)
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order?.id)
        User user = Session.get(SessionItem.USER) as User
        notaVenta.setEmpEntrego(user?.username)
        notaVenta.setHoraEntrega(new Date())
        if (notaVenta?.fechaEntrega == null) {
            notaVenta.setFechaEntrega(new Date())
        }

        println('Factura: ' + notaVenta?.getFactura())
        String idFactura = notaVenta.getFactura()
        notaVentaService.saveOrder(notaVenta)

        if (entregaInstante == false) {

            Jb trabajo = jbRepository.findOne(idFactura)
            if( trabajo == null ){
              idFactura = idFactura.replaceFirst("^0*", "")
              trabajo = jbRepository.findOne( idFactura)
            }
            trabajo.setEstado('TE')
            trabajo = jbRepository.saveAndFlush(trabajo)

            JbTrack jbTrack = new JbTrack()
            jbTrack?.rx = order?.bill
            jbTrack?.estado = 'TE'
            jbTrack?.emp = user?.username
            jbTrack?.fecha = new Date()
            jbTrack?.id_mod = '0'
            jbTrack?.id_viaje = null
            jbTrack?.obs = user?.username


            jbTrackService.saveJbTrack(jbTrack)

            jbLlamadaRepository.deleteByJbLlamada(order?.bill)

            if (trabajo?.id_grupo != null) {
                //Hoja de Proceso y Casos de Uso Pagar Saldo y Entregar - p_pagar_saldos, al entregar la venta. iv)
            }
        }
    }

    static void printOrder(String orderId) {
        printOrder(orderId, true)
    }

    static void printOrder(String orderId, boolean pNewOrder) {
        log.info("imprimiendo orden id: ${orderId}")
        if (StringUtils.isNotBlank(orderId)) {
            ticketService.imprimeVenta(orderId, pNewOrder)
        } else {
            log.warn("no se imprime orden, parametros invalidos")
        }
    }

    static List<Order> findLastOrders() {
        log.info("obteniendo ultimas ordenes")
        List<NotaVenta> results = notaVentaService.listarUltimasNotasVenta()
        return results?.collect { NotaVenta tmp ->
            Order.toOrder(tmp)
        }
    }

    static List<Order> findOrdersByParameters(Map<String, Object> params) {
        log.info("buscando ordenes por parametros: ${params}")
        List<NotaVenta> results = notaVentaService.listarNotasVentaPorParametros(params)
        log.debug("ordenes obtenidas: ${results*.id}")
        return results.collect { NotaVenta tmp ->
            Order.toOrder(tmp)
        }
    }

    static Order findOrderByTicket(String ticket) {
        log.info("buscando orden por ticket: ${ticket}")
        NotaVenta result = notaVentaService.obtenerNotaVentaPorTicket(ticket)
        return Order.toOrder(result)
    }

    static Order findOrderByIdOrder(String idOrder) {
        log.info("buscando orden por ticket: ${idOrder}")
        NotaVenta result = notaVentaService.obtenerNotaVenta(idOrder)
        return Order.toOrder(result)
    }

    static Double requestUsdRate() {
        log.info("Request USD rate")

        Double rate = 1.0
        MonedaDetalle fxrate = fxService.findActiveRate(TAG_USD)

        if (fxrate != null) {
            rate = fxrate.tipoCambio.doubleValue()

        }
        return rate
    }

    static Boolean requestUsdDisplayed() {
        if (displayUsd == null) {
            log.info("Request USD rate")

            displayUsd = fxService.requestUsdDisplayed()
        }
        return displayUsd
    }

    static SalesWithNoInventory requestConfigSalesWithNoInventory() {
        return notaVentaService.obtenerConfigParaVentasSinInventario()
    }

    static DetalleNotaVenta getDetalleNotaVenta(String idFactura, Integer idArticulo) {
        log.debug("getDetalleNotaVenta( String idFactura, Integer idArticulo )")

        DetalleNotaVenta venta = detalleNotaVentaService.obtenerDetalleNotaVenta(idFactura, idArticulo)

        return venta
    }

    static Promocion getPromocion(Integer idPromocion) {
        log.debug("getPromocion( Integer idPromocion )")
        Promocion promocion = promotionService.obtenerPromocion(idPromocion)
        return promocion
    }

    static void requestSaveAsQuote(Order pOrder, Customer pCustomer) {
        Integer pQuoteId = ServiceManager.quote.copyFromOrder(pOrder.id, pCustomer.id,
                ((User) Session.get(SessionItem.USER)).username)
        if (pQuoteId != null) {
            ticketService.imprimeCotizacion(pQuoteId)
            notaVentaService.eliminarNotaVenta(pOrder.id)
            String msg = String.format('La cotización fue registrada como: %d    ', pQuoteId)
            JOptionPane.showMessageDialog(MainWindow.instance, msg, 'Cotización', JOptionPane.INFORMATION_MESSAGE)
        }
    }

    static String requestOrderFromQuote(JPanel pComponent) {
        String orderNbr = null
        String confirm = JOptionPane.showInputDialog(MainWindow.instance, OrderPanel.MSG_INPUT_QUOTE_ID,
                OrderPanel.TXT_QUOTE_TITLE, JOptionPane.QUESTION_MESSAGE)
        if (StringUtils.trimToNull(confirm) != null) {
            Integer quoteNbr = NumberUtils.createInteger(StringUtils.trimToEmpty(confirm))
            if (quoteNbr != null) {
                Map<String, Object> result = ServiceManager.quote.toOrder(quoteNbr)
                if (result != null) {
                    orderNbr = StringUtils.trimToNull((String) result.get('orderNbr'))
                }
                if (orderNbr == null) {
                    JOptionPane.showMessageDialog(MainWindow.instance, (String) result.get('statusMessage'),
                            OrderPanel.TXT_QUOTE_TITLE, JOptionPane.ERROR_MESSAGE)
                }
            }
        }
        return orderNbr
    }

    static String requestEmployee(String pOrderId) {

        String empName = ''
        if (StringUtils.trimToNull(StringUtils.trimToEmpty(pOrderId)) != null) {
            Empleado employee = notaVentaService.obtenerEmpleadoDeNotaVenta(pOrderId)
            if (employee != null) {
                if (((User) Session.get(SessionItem.USER)).equals(employee)) {
                    empName = ((User) Session.get(SessionItem.USER)).toString()
                } else {
                    empName = User.toUser(employee).toString()
                }
            }
        }
        return empName
    }

    static void saveCustomerForOrder(String pOrderNbr, Integer pCustomerId) {
        if (StringUtils.isNotBlank(pOrderNbr)) {
            NotaVenta order = notaVentaService.obtenerNotaVenta(pOrderNbr)
            if (order != null) {
                order.idCliente = pCustomerId
                notaVentaService.saveOrder(order)
            }
        }
    }

    static Customer getCustomerFromOrder(String pOrderNbr) {
        Customer cust = null
        if (StringUtils.trimToNull(pOrderNbr) != null) {
            NotaVenta order = notaVentaService.obtenerNotaVenta(pOrderNbr)
            if (order != null) {
                cust = Customer.toCustomer(order.cliente)
            }
        }
        return cust
    }

    static Order saveOrder(Order order) {
        log.info("registrando orden id: ${order?.id}, cliente: ${order?.customer?.id}")
        if (StringUtils.isNotBlank(order?.id) && order?.customer?.id) {
            NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order.id)
            if (StringUtils.isNotBlank(notaVenta?.id)) {
                User user = Session.get(SessionItem.USER) as User
                if (StringUtils.isBlank(notaVenta.idEmpleado)) {
                    notaVenta.idEmpleado = user?.username
                }
                if (notaVenta.idCliente != null) {
                    notaVenta.idCliente = order.customer.id
                }
                notaVenta.codigo_lente = order?.dioptra
                notaVenta.observacionesNv = order.comments
                notaVenta = notaVentaService.registrarNotaVenta(notaVenta)
                return Order.toOrder(notaVenta)
            } else {
                log.warn("no se registra orden, notaVenta no existe")
            }
        } else {
            log.warn("no se registra orden, parametros invalidos")
        }
        return null
    }

    static void notifyAlert(String pTitle, String pMessage) {
        JOptionPane.showMessageDialog(MainWindow.instance, pMessage, pTitle, JOptionPane.ERROR_MESSAGE)
    }

    static Boolean isPaymentPolicyFulfilled(Order pOrder) {


        Boolean result = true
        if (pOrder.due < 0) {
            this.notifyAlert(OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Los pagos no deben ser mayores al total de la venta.')
            result = false
        } else if (pOrder.containsOphtalmic()) {

            /*
 if ( pOrder.advancePct < ( SettingsController.instance.advancePct - ZERO_TOLERANCE ) ) {
   this.notifyAlert( OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Pago menor al %Anticipo establecido.' )
   result = false
 }
          */
        } else if (pOrder.due > 0) {
            this.notifyAlert(OrderPanel.TXT_INVALID_PAYMENT_TITLE, 'Se debe cubrir el total del saldo.')
            result = false
        }
        return result
    }

    static void requestNextOrderFromCustomer(Customer pCustomer, CustomerListener pListener) {
        NotaVenta dbOrder = notaVentaService.obtenerSiguienteNotaVentaDeCliente(pCustomer.id)
        if (dbOrder != null) {
            Order o = Order.toOrder(dbOrder)
            pListener.disableUI()
            pListener.operationTypeSelected = OperationType.PAYING
            pListener.setCustomer(pCustomer)
            pListener.setOrder(o)
            pListener.enableUI()
        }
    }


    static Boolean validaEntrega(String idFactura, String idSucursal, Boolean entregaInstante) {
        String ticket = idSucursal + '-' + idFactura
        Boolean registro = true
        NotaVenta notaVenta = notaVentaService.obtenerNotaVentaPorTicket(ticket)
        if(notaVenta != null){
        Order order = Order.toOrder(notaVenta)
        List<DetalleNotaVenta> detalleVenta = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(notaVenta?.id)
        Boolean entregaBo = true
        Boolean surte = false
        if (entregaInstante == true) {
            Parametro genericoNoEntrega = parametroRepository.findOne(TipoParametro.GENERICOS_NO_ETREGABLES.value)
            ArrayList<String> genericosNoEntregables = new ArrayList<String>()
            String s = genericoNoEntrega?.valor
            StringTokenizer st = new StringTokenizer(s.trim(), ",")
            Iterator its = st.iterator()
            while (its.hasNext()) {
                genericosNoEntregables.add(its.next().toString())
            }
            Iterator iterator = detalleVenta.iterator();
            while (iterator.hasNext()) {
                DetalleNotaVenta detalle = iterator.next()

                Articulo articulo = articuloService.obtenerArticulo(detalle?.idArticulo)
                for (int a = 0; a < genericosNoEntregables.size(); a++) {
                    if (articulo?.idGenerico.trim().equals(genericosNoEntregables.get(a).trim())) {
                        entregaBo = false
                    }
                }
                if (detalle?.surte.equals('P')) {
                    surte = true
                }


            }
        }

        TmpServicios tmpServicios = tmpServiciosRepository.findbyIdFactura(notaVenta?.id)
        Boolean temp = false
        if (tmpServicios?.id_serv != null) {
            temp = true
        }
        println(surte == true)
        println(temp == true)
        println(entregaBo == true)
        //*Contacto
        if (surte == true || temp == true || entregaBo == false) {


            List<FormaContacto> result = ContactController.findByIdCliente(notaVenta?.idCliente.toInteger())
            if (result.size() == 0) {

                ContactDialog contacto = new ContactDialog(notaVenta)
                contacto.activate()

            } else {
                ContactClientDialog contactoCliente = new ContactClientDialog(notaVenta)
                contactoCliente.activate()

                if (contactoCliente.formaContactoSeleted != null) {

                    FormaContacto formaContacto = contactoCliente.formaContactoSeleted
                    formaContacto?.rx = notaVenta?.factura
                    formaContacto?.fecha_mod = new Date()
                    formaContacto?.id_cliente = notaVenta?.idCliente
                    formaContacto?.id_sucursal = notaVenta?.idSucursal
                    formaContacto?.observaciones =  contactoCliente.formaContactoSeleted?.observaciones != '' ? contactoCliente.formaContactoSeleted?.observaciones : ' '
                    formaContacto?.id_tipo_contacto = contactoCliente.formaContactoSeleted?.tipoContacto?.id_tipo_contacto
                    ContactController.saveFormaContacto(formaContacto)


                }
            }


        }
        //*Contacto


        if ((order?.total - order?.paid) == 0 && entregaBo == true) {
            Boolean fechaC = true
            if (entregaInstante == false) {
                SimpleDateFormat fecha = new SimpleDateFormat("dd/MMMM/yyyy")
                String fechaVenta = fecha.format(notaVenta?.fechaHoraFactura).toString()
                String ahora = fecha.format(new Date())
                if (fechaVenta.equals(ahora)) {
                    fechaC = false
                }
            }

            if (fechaC == true) {
                OrderController.insertaEntrega(order, entregaInstante)
            } else {
                JOptionPane.showMessageDialog(null, "No se puede entregar trabajo hoy mismo")
            }
        } else {
            if (entregaInstante == false) {
                JOptionPane.showMessageDialog(null, "La nota tiene saldo pendiente por cubrir. No se puede entregar trabajo")
            }
        }
    }else{
            registro = false
        }
        return registro
    }

    static void creaJb(String idFactura, Boolean cSaldo) {

        NotaVenta notaVenta = notaVentaService.obtenerNotaVentaPorTicket(idFactura)
        List<DetalleNotaVenta> detalleVenta = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(notaVenta?.id)
        Boolean creaJB = false
        String articulos = ''
        String surte = ''
        String tipoJb = ''
        Boolean genericoD = false
        Iterator iterator = detalleVenta.iterator();
        while (iterator.hasNext()) {

            DetalleNotaVenta detalle = iterator.next()
            Articulo articulo = articuloService.obtenerArticulo(detalle?.idArticulo)

            articulos = articulos + articulo?.articulo + ', '

            if (articulo?.idGenerico.trim().equals('A') || articulo?.idGenerico.trim().equals('E')) {
                surte = detalle?.surte
            }

            if (articulo?.idGenerico.trim().equals('D')) {
                genericoD = true
            }

            if (articulo?.idGenerico.trim().equals('B') || articulo?.idGenerico.trim().equals('C') || articulo?.idGenerico.trim().equals('H')) {
                creaJB = true
                if (articulo?.idGenerico.trim().equals('C') || articulo?.idGenerico.trim().equals('H')) {
                    tipoJb = 'LC'
                } else if (articulo?.idGenerico.trim().equals('B')) {
                    tipoJb = 'LAB'
                }

            }
            String surt = StringUtils.trimToEmpty(detalle?.surte) != '' ? detalle?.surte.trim() : ''
            if (surt.equals('P')) {
                creaJB = true
            }

        }

        TmpServicios tmpServicios = tmpServiciosRepository.findbyIdFactura(notaVenta?.id)
        if (tmpServicios?.id_serv != null) {
            creaJB = true
        }

        if (creaJB == true) {
            Jb jb = jbRepository.findOne(notaVenta?.factura)
            println('JB: ' + jb?.rx)

            Jb nuevoJb = new Jb()
            JbTrack nuevojbTrack = new JbTrack()

            if (jb?.rx == null) {

                nuevoJb?.rx = notaVenta?.factura
                nuevoJb?.estado = 'PE'
                nuevoJb?.id_cliente = notaVenta?.idCliente
                nuevoJb?.emp_atendio = notaVenta?.empleado?.id
                nuevoJb?.fecha_promesa = notaVenta?.fechaPrometida
                nuevoJb?.num_llamada = 0
                nuevoJb?.material = articulos
                nuevoJb?.surte = surte
                nuevoJb?.saldo = notaVenta.ventaNeta - notaVenta?.sumaPagos
                nuevoJb?.jb_tipo = tipoJb
                nuevoJb?.cliente = notaVenta?.cliente?.nombreCompleto
                nuevoJb?.fecha_venta = notaVenta?.fechaHoraFactura




                nuevojbTrack?.rx = notaVenta?.factura
                nuevojbTrack?.estado = 'PE'
                nuevojbTrack?.emp = notaVenta?.empleado?.id
                nuevojbTrack?.obs = articulos

                println('LC: ' + nuevoJb?.jb_tipo)
                println('LC: ' + nuevoJb?.jb_tipo.trim().equals('LC'))
                if (nuevoJb?.jb_tipo.trim().equals('LC')) {

                    nuevoJb?.estado = 'EP'
                    nuevoJb?.id_viaje = '8'

                    JbTrack nuevoJbTrack2 = new JbTrack()
                    nuevoJbTrack2?.rx = notaVenta?.factura
                    nuevoJbTrack2?.estado = 'EP'
                    nuevoJbTrack2?.obs = '8'
                    nuevoJbTrack2?.id_viaje = '8'
                    nuevoJbTrack2?.emp = notaVenta?.empleado?.id
                    nuevoJbTrack2?.fecha = new Date()
                    nuevoJbTrack2?.id_mod = '0'
                    println('LC: ' + nuevoJbTrack2?.id_viaje)
                    nuevoJbTrack2 = jbTrackService.saveJbTrack(nuevoJbTrack2)

                }

                Parametro convenioNomina = parametroRepository.findOne(TipoParametro.CONV_NOMINA.value)


                String s = convenioNomina?.valor
                StringTokenizer st = new StringTokenizer(s.trim(), ",")
                Iterator its = st.iterator()
                Boolean convenio = false
                while (its.hasNext()) {
                    if (its.next().toString().equals(notaVenta?.idConvenio)) {
                        convenio = true
                    }
                }

                if (convenio == true) {
                    nuevoJb?.estado = 'RTN'
                    if (genericoD == true) {
                        nuevoJb?.jb_tipo = 'EMA'
                    } else {
                        nuevoJb?.jb_tipo = 'EMP'
                    }

                }


            }

            if (cSaldo == true) {
                nuevoJb?.estado = 'RTN'
                nuevojbTrack?.estado = 'RTN'
                nuevojbTrack?.obs = 'Factura con Saldo'
            }

            nuevoJb?.fecha_mod = new Date()
            nuevoJb?.id_mod = '0'
            nuevojbTrack?.fecha = new Date()
            nuevojbTrack?.id_mod = '0'
            nuevoJb = jbRepository.saveAndFlush(nuevoJb)
            nuevojbTrack = jbTrackService.saveJbTrack(nuevojbTrack)

        }

    }

    static DescuentoClave descuentoClavexId(String idDescuentoClave) {
        DescuentoClave descuentoClave = descuentoClaveRepository.findOne(idDescuentoClave)
        return descuentoClave
    }

    static Boolean surteEnabled(String idGenerico) {
        Generico generico = genericoRepository.findOne(idGenerico)
        return generico?.inventariable
    }


    static List<String> surteOption(String idGenerico, String surte) {
        Generico generico = genericoRepository.findOne(idGenerico)

        List<String> surteOption = new ArrayList<String>()
        surteOption.add(surte)
        String s = generico?.surte
        StringTokenizer st = new StringTokenizer(s.trim(), ",")
        Iterator its = st.iterator()
        while (its.hasNext()) {
            if (!its.next().toString().trim().equals(surte)) {
                surteOption.add(its.next().toString())
            }
        }

        return surteOption
    }


    static SurteSwitch surteCallWS(Branch branch, Item item, String surte, Order order) {
        Boolean agregaArticulo = true
        Boolean surteSucursal = true
        SurteSwitch surteSwitch = new SurteSwitch()
        surteSwitch?.surte = surte
        Precio precio = precioRepository.findbyArt(item?.name.trim())

        if (item?.type?.trim().equals('A') && precio?.surte?.trim().equals('P')) {
            AcusesTipo acusesTipo = acusesTipoRepository.findOne('AUT')
            String url = acusesTipo?.pagina + '?id_suc=' + branch?.id.toString().trim() + '&id_col=' + item?.color?.trim() + '&id_art=' + item?.name.toString().trim()
            println(url)
            String resultado = callWS(url)
            println(resultado)
            int index
            try {
                index = resultado.indexOf('|')
            } catch (ex) {
                index = 1
            }

            String condicion = resultado.substring(0, index)
            if (condicion.trim().equals('Si')) {

                String contenido = resultado + '|' + item?.id + '|' + item?.color + '|' + 'facturacion'
                Date date = new Date()
                SimpleDateFormat formateador = new SimpleDateFormat("hhmmss")
                String nombre = formateador.format(date)
                generaAcuse(contenido, nombre)

                surteSwitch.surte = 'P'
            } else if (condicion.trim().equals('No')) {
                Integer question = JOptionPane.showConfirmDialog(new JDialog(), '¿Desea Continuar con la venta?', 'Almacen Central sin Existencias',
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
                if (question == 0) {
                    surteSucursal = false
                } else {
                    agregaArticulo = false
                }
            } else {
                notifyAlert('Almacen Central no Responde', 'Contacte a Soporte Tecnico')
                agregaArticulo = false
            }


        }

        surteSwitch.setAgregaArticulo(agregaArticulo)
        surteSwitch.setSurteSucursal(surteSucursal)

        return surteSwitch
    }

    static void insertaAcuseAPAR(Order order, Branch branch) {

        List<DetalleNotaVenta> listarDetallesNotaVentaPorIdFactura = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(order?.id)
        String parte = ''
        int rx = 0
        Item item =  new Item()
        Boolean insertarAcuse = false
        Iterator iterator = listarDetallesNotaVentaPorIdFactura.iterator();
        while (iterator.hasNext()) {
            DetalleNotaVenta detalleNotaVenta = new DetalleNotaVenta()
            detalleNotaVenta = iterator.next()
            if (detalleNotaVenta?.articulo?.idGenerico?.trim().equals('B')) {
                rx = 1
            }
            if (detalleNotaVenta?.idTipoDetalle?.trim().equals('VD') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('VI.') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('FT') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('LD') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('LI') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('CI') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('CD') ||
                    detalleNotaVenta?.idTipoDetalle?.trim().equals('REM')
            ) {
                parte = parte + detalleNotaVenta?.idTipoDetalle?.trim() + ','
            }

            if (detalleNotaVenta?.surte?.trim().equals('P') && detalleNotaVenta?.articulo?.idGenerico?.trim().equals('A')) {
                insertarAcuse = true
                item = Item.toItem(detalleNotaVenta?.articulo)
            }




        }

        if (insertarAcuse) {

            String contenidoAPAR = "parteVal=" + parte
            contenidoAPAR = contenidoAPAR + "|facturaVal=" + order?.bill
            contenidoAPAR = contenidoAPAR + "|rxVal=" + rx
            contenidoAPAR = contenidoAPAR + "|id_colVal=" + item?.color
            contenidoAPAR = contenidoAPAR + "|id_sucVal=" + branch?.id
            contenidoAPAR = contenidoAPAR + "|id_artVal=" + item?.name
            contenidoAPAR = contenidoAPAR + "|id_acuseVal=" + (acuseRepository?.nextIdAcuse() +1).toString() + '|'

            Acuse acuseAPAR = new Acuse()
            acuseAPAR?.contenido = contenidoAPAR
            acuseAPAR?.idTipo = 'APAR'
            acuseAPAR?.intentos = 0

            acuseRepository.saveAndFlush(acuseAPAR)
            insertarAcuse = false
        }
    }

    static void generaAcuse(String contenido, String nombre) {
        try {
            Parametro ruta = parametroRepository.findOne(TipoParametro.ARCHIVO_CONSULTA_WEB.value)
            File archivo = new File(ruta?.valor, nombre.toString())
            BufferedWriter out = new BufferedWriter(new FileWriter(archivo))
            out.write(contenido)
            out.close()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    static String callUrlMethod(String url) {
        String resultado = new String()
        def urlTexto = url
        def resp = urlTexto?.toURL()
        resp = resp.text

        List<String> htmlList = new ArrayList<String>()

        String s = resp?.replaceAll("[\n\r\t]", "")

        println(s)
        StringTokenizer st = new StringTokenizer(s.trim(), ">")
        Iterator its = st.iterator()
        int ini = 0
        Boolean xx = false

        while (its.hasNext()) {
            htmlList.add(its.next().toString() + ">")
            if (xx == true) {

                int index = htmlList.get(ini).indexOf('<')

                resultado = htmlList.get(ini).substring(0, index)

                xx = false

            }
            if (htmlList.get(ini).trim().equals('<XX>')) {
                xx = true
            }
            ini = ini + 1
        }
        return resultado
    }




  static  String callWS(String url) {
      ExecutorService executor = Executors.newFixedThreadPool(1)
        String respuesta = new String()
        int timeoutSecs = 15
        final Future<?> future = executor.submit(new Runnable() {

            public void run() {

                try {

                    respuesta = callUrlMethod(url)

                } catch (Exception e) {

                    throw new RuntimeException(e)

                }

            }

        })


        try {

            future.get(timeoutSecs, TimeUnit.SECONDS)

        } catch (Exception e) {

            future.cancel(true)
            respuesta = ''
            log.warn("encountered problem while doing some work", e)

        }

        return respuesta

    }


    static String armazonString(String idNotaVenta) {
        String armazonString = ''
        List<DetalleNotaVenta> detalleVenta = detalleNotaVentaService.listarDetallesNotaVentaPorIdFactura(idNotaVenta)
        Iterator iterator = detalleVenta.iterator();
        while (iterator.hasNext()) {
            DetalleNotaVenta detalle = iterator.next()

            if (detalle?.articulo?.idGenerico.trim().equals('A')) {
                armazonString = detalle?.articulo?.articulo.trim()
            }

        }
        return armazonString
    }

    static validaSurtePorGenerico( Order order ){
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order.id)
        notaVentaService.validaSurtePorGenericoInventariable( notaVenta )
    }


    static String obtieneTiposClientesActivos( ){
        return Registry.activeCustomers
    }


    static void saveSuyo(Order order, User user, String dejo, String instrucciones, String condiciones, String serv) {
        TmpServicios servicios = new TmpServicios()
        servicios?.id_factura = order?.id
        servicios?.fecha_prom = new Date()
        servicios?.emp = user?.username
        servicios?.id_cliente = order?.customer?.id
        servicios?.cliente = order?.customer?.name + ' ' + order?.customer?.fathersName + ' ' + order?.customer?.mothersName
        servicios?.condicion = condiciones
        servicios?.dejo = dejo
        servicios?.instruccion = instrucciones
        servicios?.servicio = serv
        tmpServiciosRepository.saveAndFlush(servicios)
        NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(order?.id)
        notaVenta?.observacionesNv = condiciones
        notaVentaService.saveOrder(notaVenta)
    }

    static void printSuyo(Order order, User user) {
        TmpServicios servicios = tmpServiciosRepository.findOne( tmpServiciosRepository.tmpExiste(order?.id))
        JbNotas jbNotas = new JbNotas()
        jbNotas?.id_nota = order?.bill.toInteger()
        jbNotas?.id_cliente = order?.customer?.id
        jbNotas?.cliente = order?.customer?.name + ' ' + order?.customer?.fathersName + ' ' + order?.customer?.mothersName
        jbNotas?.dejo = servicios?.dejo
        jbNotas?.instruccion =  servicios?.instruccion
        jbNotas?.emp = user?.username
        jbNotas?.servicio = servicios?.servicio
        jbNotas?.condicion = servicios?.condicion
        jbNotas?.fecha_prom = servicios?.fecha_prom
        jbNotas?.fecha_orden = order?.date
        jbNotas?.fecha_mod = new Date()
        jbNotas?.tipo_serv = 'RECEPCION'
        jbNotas?.id_mod = '0'

        jbNotas = jbNotasRepository.saveAndFlush(jbNotas)


        ticketService.imprimeSuyo(order?.id, jbNotas)
    }



    static Boolean revisaTmpservicios(String idNotaVenta) {
        Boolean existe = false
        Integer idTmpServicio = tmpServiciosRepository.tmpExiste(idNotaVenta)
        if (idTmpServicio != null) {
            existe = true
        }
        return existe
    }

    static ArrayList<String> findAllServices() {
        ArrayList<String> list = new ArrayList<>()
        List<JbServicios> jbServiciosList = jbServiciosRepository.findAll()
        Iterator iterator = jbServiciosList.iterator()
        while (iterator.hasNext()) {

            list.add(iterator.next().servicio)
        }
        return list
    }



    static Boolean validOnlyOnePackage( List<OrderItem> lstItems, Integer idItem ){
      List<Integer> lstIds = new ArrayList<Integer>()
      for(OrderItem item : lstItems){
        lstIds.add( item.item.id )
      }
      Boolean unPaquete = articuloService.validaUnSoloPaquete( lstIds, idItem )
      return unPaquete
    }


}
