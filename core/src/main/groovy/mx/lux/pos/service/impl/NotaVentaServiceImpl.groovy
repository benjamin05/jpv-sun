package mx.lux.pos.service.impl

import com.mysema.query.BooleanBuilder
import com.mysema.query.types.OrderSpecifier
import com.mysema.query.types.Predicate
import groovy.util.logging.Slf4j
import mx.lux.pos.model.*
import mx.lux.pos.repository.*
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.NotaVentaService
import mx.lux.pos.service.business.EliminarNotaVentaTask
import mx.lux.pos.service.business.Registry
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import java.text.NumberFormat

@Slf4j
@Service( 'notaVentaService' )
@Transactional( readOnly = true )
class NotaVentaServiceImpl implements NotaVentaService {

  private static final String DATE_TIME_FORMAT = 'dd-MM-yyyy HH:mm:ss'
  private static final String TAG_SURTE_SUCURSAL = 'S'
  private static final String TAG_PAGO_CUPON = 'C'
  private static final String TAG_REUSO = 'R'
  private static final String TAG_GENERICOS_INVENTARIABLES = 'A,E'
  private static final String TAG_TIPO_NOTA_VENTA = 'F'
  private static final String TAG_NOTA_CANCELADA = 'T'
  private static final String TAG_TRANSFERENCIA = 'TR'
  private static final String TAG_GENERICOS_B = 'B'

  @Resource
  private NotaVentaRepository notaVentaRepository

  @Resource
  private DetalleNotaVentaRepository detalleNotaVentaRepository

  @Resource
  private PagoRepository pagoRepository

  @Resource
  private SucursalRepository sucursalRepository

  @Resource
  private ArticuloRepository articuloRepository

  @Resource
  private PrecioRepository precioRepository

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private ModificacionRepository modificacionRepository

  @Resource
  private FacturasImpuestosRepository facturasImpuestosRepository

  @Resource
  private JbRepository jbRepository

  @Resource
  private JbTrackRepository jbTrackRepository

  @Override
  NotaVenta obtenerNotaVenta( String idNotaVenta ) {
    log.info( "obteniendo notaVenta: ${idNotaVenta}" )
    if ( StringUtils.isNotBlank( idNotaVenta ) ) {
      NotaVenta notaVenta = notaVentaRepository.findOne( idNotaVenta )
      log.debug( "obtiene notaVenta id: ${notaVenta?.id}," )
      log.debug( "fechaHoraFactura: ${notaVenta?.fechaHoraFactura?.format( DATE_TIME_FORMAT )}" )
      return notaVenta
    } else {
      log.warn( 'no se obtiene notaVenta, parametros invalidos' )
    }
    return null
  }
    @Override
    @Transactional
    NotaVenta notaVentaxRx(Integer rx){
     return  notaVentaRepository.notaVentaxRx(rx)
    }


    @Override
  @Transactional
  NotaVenta abrirNotaVenta(String clienteID, String empleadoID ) {
    log.info( 'abriendo nueva notaVenta' )
    Parametro parametro = new Parametro()
      parametro.setValor(clienteID)
      //Cambiar el parametro por clienteID

    NotaVenta notaVenta = new NotaVenta(
        id: notaVentaRepository.getNotaVentaSequence(),
        idSucursal: sucursalRepository.getCurrentSucursalId(),
        idCliente: parametro?.valor?.isInteger() ? parametro.valor.toInteger() : null
    )
      notaVenta.setIdEmpleado(empleadoID)
    try {
      notaVenta = notaVentaRepository.save( notaVenta )
      log.info( "notaVenta registrada id: ${notaVenta?.id}" )
      return notaVenta
    } catch ( ex ) {
      log.error( "problema al registrar notaVenta: ${notaVenta?.dump()}", ex )
    }
    return null
  }

  @Override
  @Transactional
  NotaVenta registrarNotaVenta( NotaVenta notaVenta ) {
    log.info( "registrando notaVenta id: ${notaVenta?.id}," )
    log.info( "fechaHoraFactura: ${notaVenta?.fechaHoraFactura?.format( DATE_TIME_FORMAT )}" )
    if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
      String idNotaVenta = notaVenta.id
      if ( notaVentaRepository.exists( idNotaVenta ) ) {
        notaVenta.idSucursal = sucursalRepository.getCurrentSucursalId()
        BigDecimal total = BigDecimal.ZERO
        List<DetalleNotaVenta> detalles = detalleNotaVentaRepository.findByIdFactura( idNotaVenta )
        detalles?.each { DetalleNotaVenta detalleNotaVenta ->
            BigDecimal precio = detalleNotaVenta?.precioUnitFinal ?: 0
            Integer cantidad = detalleNotaVenta?.cantidadFac ?: 0
            BigDecimal subtotal = precio.multiply( cantidad )
            total = total.add( subtotal )
        }

        BigDecimal pagado = BigDecimal.ZERO
        List<Pago> pagos = pagoRepository.findByIdFactura( idNotaVenta )
        pagos?.each { Pago pago ->
          BigDecimal monto = pago?.monto ?: 0
          pagado = pagado.add( monto )
        }
        log.debug( "ventaNeta: ${notaVenta.ventaNeta} -> ${total}" )
        log.debug( "ventaTotal: ${notaVenta.ventaTotal} -> ${total}" )
        log.debug( "sumaPagos: ${notaVenta.sumaPagos} -> ${pagado}" )
        BigDecimal diferencia = notaVenta?.ventaNeta?.subtract(total)
        if( notaVenta?.montoDescuento?.compareTo(BigDecimal.ZERO) > 0 &&
                ((notaVenta?.ventaNeta?.subtract(total) < new BigDecimal(0.02)) && (notaVenta?.ventaNeta?.subtract(total) > new BigDecimal(-0.02))) ){
          log.debug( "redondeo monto total" )
          DetalleNotaVenta det =  detalles.first()
          BigDecimal monto = det.precioUnitFinal.add(diferencia)
          if( diferencia.compareTo(BigDecimal.ZERO) > 0 ){
            det.setPrecioUnitFinal( monto )
            detalleNotaVentaRepository.save( det )
            detalleNotaVentaRepository.flush()
          }
        } else {
          notaVenta.ventaNeta = total
          notaVenta.ventaTotal = total
        }

        notaVenta.sumaPagos = pagado
        notaVenta.tipoNotaVenta = TAG_TIPO_NOTA_VENTA
        try {
          notaVenta = notaVentaRepository.save( notaVenta )
          notaVentaRepository.flush()
          log.info( "notaVenta registrada id: ${notaVenta?.id}" )

        } catch ( ex ) {
          log.error( "problema al registrar notaVenta: ${notaVenta?.dump()}", ex )
        }
      } else {
        log.warn( "no se registra notaVenta, id no existe" )
      }
    } else {
      log.warn( "no se registra notaVenta, parametros invalidos" )
    }
    return notaVenta
  }

  private DetalleNotaVenta establecerPrecios( DetalleNotaVenta detalle ) {
    log.debug( "estableciendo precios para detalleNotaVenta articulo: ${detalle?.idArticulo}" )
    if ( detalle?.idArticulo ) {
      Articulo articulo = articuloRepository.findOne( detalle.idArticulo )
      log.debug( "obtiene articulo id: ${articulo?.id}, codigo: ${articulo?.articulo}, color: ${articulo?.codigoColor}" )
      if ( articulo?.id ) {
        List<Precio> precios = precioRepository.findByArticulo( articulo.articulo )
        log.debug( "obtiene lista de precios ${precios*.lista}" )
        if ( precios?.any() ) {
          Precio precioLista = precios.find { Precio tmp ->
            'L'.equalsIgnoreCase( tmp?.lista )
          }
          log.debug( "precio lista: ${precioLista?.dump()}" )
          BigDecimal lista = precioLista?.precio ?: BigDecimal.ZERO
          Precio precioOferta = precios.find { Precio tmp ->
            'O'.equalsIgnoreCase( tmp?.lista )
          }
          log.debug( "precio oferta: ${precioOferta?.dump()}" )
          BigDecimal oferta = precioOferta?.precio ?: BigDecimal.ZERO
          BigDecimal unitario = oferta && ( oferta < lista ) ? oferta : lista
          detalle.precioCalcLista = lista
          detalle.precioCalcOferta = oferta
          detalle.precioUnitLista = unitario
          detalle.precioUnitFinal = unitario
          detalle.precioFactura = unitario
          detalle.precioConv = BigDecimal.ZERO
          log.debug( "detalleNotaVenta actualizado: ${detalle.dump()}" )
        } else {
          log.warn( 'no se establecen precios, lista de precios vacia' )
        }
      } else {
        log.warn( 'no se establecen precios, articulo invalido' )
      }
    } else {
      log.warn( 'no se establecen precios, parametros invalidos' )
    }
    return detalle
  }

  @Override
  @Transactional
  NotaVenta registrarDetalleNotaVentaEnNotaVenta( String idNotaVenta, DetalleNotaVenta detalleNotaVenta ) {
    log.info( "registrando detalleNotaVenta id: ${detalleNotaVenta?.id} idArticulo: ${detalleNotaVenta?.idArticulo}" )
    log.info( "en notaVenta id: ${idNotaVenta}" )
    NotaVenta notaVenta = obtenerNotaVenta( idNotaVenta )
    if ( StringUtils.isNotBlank( notaVenta?.id ) && detalleNotaVenta?.idArticulo ) {
      detalleNotaVenta.idFactura = idNotaVenta
      detalleNotaVenta.idSucursal = sucursalRepository.getCurrentSucursalId()
      DetalleNotaVenta tmp = detalleNotaVentaRepository.findByIdFacturaAndIdArticulo( idNotaVenta, detalleNotaVenta.idArticulo )
      log.debug( "obtiene detalleNotaVenta existente: ${tmp?.dump()}" )
      if ( tmp?.id ) {
        log.debug( "actualizando detalleNotaVenta con id: ${tmp.id} cantidadFac: ${tmp.cantidadFac}" )
        detalleNotaVenta.id = tmp.id
        detalleNotaVenta.cantidadFac += tmp.cantidadFac
        log.debug( "actualizados cantidadFac: ${detalleNotaVenta.cantidadFac}" )
      } else {
        log.debug( "registrando nuevo detalleNotaVenta" )
      }
      detalleNotaVenta = establecerPrecios( detalleNotaVenta )
      try {
        detalleNotaVenta = detalleNotaVentaRepository.save( detalleNotaVenta )
        log.debug( "detalleNotaVenta registrado id: ${detalleNotaVenta.id}" )
        return registrarNotaVenta( notaVenta )
      } catch ( ex ) {
        log.error( "problema al registrar detalleNotaVenta: ${detalleNotaVenta?.dump()}", ex )
      }
    } else {
      log.warn( "no se registra detalleNotaVenta, parametros invalidos" )
    }
    return null
  }

  @Override
  @Transactional
  NotaVenta eliminarDetalleNotaVentaEnNotaVenta( String idNotaVenta, Integer idArticulo ) {
    log.info( "eliminando detalleNotaVenta idArticulo: ${idArticulo} de notaVenta id: ${idNotaVenta}" )
    if ( idArticulo && StringUtils.isNotBlank( idNotaVenta ) ) {
      DetalleNotaVenta detalle = detalleNotaVentaRepository.findByIdFacturaAndIdArticulo( idNotaVenta, idArticulo )
      if ( detalle?.id ) {
        log.debug( "obtiene detalleNotaVenta id: ${detalle.id}" )
        NotaVenta notaVenta = obtenerNotaVenta( idNotaVenta )
        if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
          detalleNotaVentaRepository.delete( detalle.id )
          log.debug( "detalleNotaVenta eliminado" )
          return registrarNotaVenta( notaVenta )
        } else {
          log.warn( "no se elimina detalleNotaVenta, no existe notaVenta id: ${idNotaVenta}" )
        }
      } else {
        log.warn( "no se elimina detalleNotaVenta, no existe con idNotaVenta: ${idNotaVenta} idArticulo: ${idArticulo}" )
      }
    } else {
      log.warn( "no se elimina detalleNotaVenta, parametros invalidos" )
    }
    return null
  }

  @Override
  @Transactional
  Pago registrarPagoEnNotaVenta( String idNotaVenta, Pago pago ) {
    log.info( "registrando pago id: ${pago?.id} idFormaPago: ${pago?.idFormaPago} monto: ${pago?.monto}" )
    log.info( "en notaVenta id: ${idNotaVenta}" )
    NotaVenta notaVenta = obtenerNotaVenta( idNotaVenta )
    if ( StringUtils.isNotBlank( notaVenta?.id ) && StringUtils.isNotBlank( pago?.idFormaPago ) && pago?.monto ) {
      String formaPago = pago.idFormaPago
      if ( 'ES'.equalsIgnoreCase( formaPago ) ) {
        formaPago = 'EFM'
      } else if ( 'TS'.equalsIgnoreCase( formaPago ) ) {
        formaPago = 'TCM'
      }
      log.debug( "forma pago definida: ${formaPago}" )
      Date fechaActual = new Date()
      pago.idFormaPago = formaPago
      pago.idFactura = idNotaVenta
      pago.idSucursal = sucursalRepository.getCurrentSucursalId()
      pago.tipoPago = DateUtils.isSameDay( notaVenta.fechaHoraFactura ?: fechaActual, fechaActual ) ? 'a' : 'l'
      log.debug( "obteniendo existencia de pago con id: ${pago.id}" )
      Pago tmp = pagoRepository.findOne( pago.id ?: 0 )
      if ( tmp?.id ) {
        log.debug( "pago ya registrado, no se puede modificar" )
      } else {
        log.debug( "registrando pago con monto: ${pago.monto}" )
        try {
          pago = pagoRepository.save( pago )
          log.debug( "pago registrado id: ${pago.id}" )
            registrarNotaVenta( notaVenta )
            return  pago
        } catch ( ex ) {
          log.error( "problema al registrar pago: ${pago?.dump()}", ex )
        }
      }
    } else {
      log.warn( "no se registra pago, parametros invalidos" )
    }
    return null
  }

  @Override
  @Transactional
  NotaVenta eliminarPagoEnNotaVenta( String idNotaVenta, Integer idPago ) {
    log.info( "eliminando pago id: ${idPago} idFactura: ${idNotaVenta}" )
    if ( idPago && StringUtils.isNotBlank( idNotaVenta ) ) {
      Pago pago = pagoRepository.findOne( idPago )
      if ( pago?.id ) {
        log.debug( "obtiene pago id: ${pago.id} idFormaPago: ${pago.idFormaPago} monto: ${pago.idFormaPago}" )
        NotaVenta notaVenta = obtenerNotaVenta( idNotaVenta )
        if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
          pagoRepository.delete( pago.id )
          pagoRepository.flush()
          log.debug( "pago eliminado" )
          return registrarNotaVenta( notaVenta )
        } else {
          log.warn( "no se elimina pago, no existe notaVenta id: ${idNotaVenta}" )
        }
      } else {
        log.warn( "no se elimina pago, no existe con id: ${idPago}" )
      }
    } else {
      log.warn( "no se elimina pago, parametros invalidos" )
    }
    return null
  }
    @Transactional
    NotaVenta saveFrame(String idNotaVenta, String opciones, String forma) {

        NotaVenta rNotaVenta = obtenerNotaVenta(idNotaVenta)

                println('Material: ' + opciones)
                println('Acabado: '+ forma)
                rNotaVenta?.setUdf2(opciones)
                rNotaVenta?.setUdf3(forma)
        try{
          println rNotaVenta.dump()
          rNotaVenta =  notaVentaRepository.save( rNotaVenta )
          notaVentaRepository.flush()
        } catch ( Exception e ){
            println e
        }
        return rNotaVenta
    }


    @Transactional
    void saveProDate(NotaVenta rNotaVenta, Date fechaPrometida){

        if ( StringUtils.isNotBlank( rNotaVenta.id) ) {
            if ( notaVentaRepository.exists( rNotaVenta.id ) ) {

                rNotaVenta.setFechaPrometida(fechaPrometida)


                registrarNotaVenta( rNotaVenta )
            } else {
                log.warn( "id no existe" )
            }
        } else {
            log.warn( "No hay receta" )
        }
    }

    @Transactional
    void saveRx(NotaVenta rNotaVenta, Integer receta){
        if ( StringUtils.isNotBlank( rNotaVenta.id) ) {
            if ( notaVentaRepository.exists( rNotaVenta.id ) ) {
                rNotaVenta.setReceta(receta)
             registrarNotaVenta( rNotaVenta )
            } else {
                log.warn( "id no existe" )
            }
        } else {
            log.warn( "No hay receta" )
        }
    }

  @Override
  @Transactional
  NotaVenta cerrarNotaVenta( NotaVenta notaVenta ) {
    log.info( "cerrando notaVenta id: ${notaVenta?.id}" )
    if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
      String idNotaVenta = notaVenta.id
      if ( notaVentaRepository.exists( idNotaVenta ) ) {
        Boolean subtypeS = false
        for(DetalleNotaVenta det : notaVenta.detalles){
          if(det.articulo.subtipo.startsWith('S')){
            subtypeS = true
          }
        }
        if( subtypeS && notaVenta.codigo_lente != null && notaVenta.codigo_lente.trim().length() > 0 ){
          String dioptra = notaVenta.codigo_lente
          String dioptraTmp = dioptra.substring( 0, dioptra.length()-1 )
          dioptra = dioptraTmp+'T'
          notaVenta.codigo_lente = dioptra
        }
        Date fecha = new Date()
        String factura = String.format( "%06d", notaVentaRepository.getFacturaSequence() )
        notaVenta.factura = factura
        notaVenta.tipoNotaVenta = 'F'
        notaVenta.tipoDescuento = 'N'
        notaVenta.tipoEntrega = 'S'
        notaVenta.setfExpideFactura( true )
        //notaVenta.fechaEntrega = notaVenta.fechaEntrega ?: fecha
        //notaVenta.horaEntrega = notaVenta.horaEntrega ?: fecha
        notaVenta.fechaPrometida = notaVenta.fechaPrometida ?: fecha
        return registrarNotaVenta( notaVenta )
      } else {
        log.warn( "no se cierra notaVenta, id no existe" )
      }
    } else {
      log.warn( "no se cierra notaVenta, parametros invalidos" )
    }
    return null
  }

  @Override
  List<NotaVenta> listarUltimasNotasVenta( ) {
    log.info( "listando ultimas notasVenta" )
    List<NotaVenta> results = notaVentaRepository.findByFacturaNotEmptyLimitingLatestResults( 10 )
    return results?.any() ? results : [ ]
  }

  private Predicate generarPredicadoTicket( String ticket ) {
    log.info( "generando predicado para busqueda de notaVenta con ticket: ${ticket}" )
    List<String> tokens = StringUtils.splitPreserveAllTokens( ticket, '-' )
    if ( StringUtils.isNotBlank( ticket ) && tokens?.size() >= 2 ) {
      String centroCostos = StringUtils.trimToEmpty( tokens.get( 0 ) )
      String factura = StringUtils.trimToEmpty( tokens.get( 1 ) )
      log.debug( "ticket con centro de costos: ${centroCostos} y factura: ${factura}" )
      if ( factura.length() > 0 && centroCostos.length() > 0 ) {
        QNotaVenta qNotaVenta = QNotaVenta.notaVenta
        BooleanBuilder builder = new BooleanBuilder( qNotaVenta.factura.eq( factura ) )
        builder.and( qNotaVenta.sucursal.centroCostos.eq( centroCostos ) )
        return builder
      } else {
        log.warn( 'no se genera predicado, factura y/o centro de costos invalidos' )
      }
    } else {
      log.warn( 'no se genera predicado, parametros invalidos' )
    }
    return null
  }

  @Override
  List<NotaVenta> listarNotasVentaPorParametros( Map<String, Object> parametros ) {
    log.info( "listando notasVenta por parametros: ${parametros}" )
    if ( parametros?.any() ) {

        println(parametros.dateFrom as Date)
        println(parametros.dateTo as Date)
        println(parametros.folio)
        println(parametros.ticket)
        println(parametros.employee)
      Date dateFrom = parametros.dateFrom as Date
      Date dateTo = parametros.dateTo as Date
      String folio = parametros.folio
      String ticket = parametros.ticket
      String employee = parametros.employee
      String factura = ''
      QNotaVenta qNotaVenta = QNotaVenta.notaVenta
      BooleanBuilder builder = new BooleanBuilder()
      if(ticket.trim() != ''){
        String[] ticketValid = ticket.split('-')
        if(ticketValid.length > 1){
          dateFrom = null
          dateTo = null
          factura = ticketValid[1]
        }
      }
      if ( dateFrom && dateTo ) {
        dateTo = new Date( dateTo.next().time - 1 )
        log.debug( "fecha inicio: ${dateFrom?.format( DATE_TIME_FORMAT )}" )
        log.debug( "fecha fin: ${dateTo?.format( DATE_TIME_FORMAT )}" )
          if(!StringUtils.isNotBlank( folio )){
          builder.and( qNotaVenta.fechaHoraFactura.between( dateFrom, dateTo ) )
      }
      }
      if ( StringUtils.isNotBlank( folio ) ) {
        log.debug( "folio: ${folio}" )
        builder.and( qNotaVenta.id.eq( folio ) )
      }
      Predicate predicate = generarPredicadoTicket( ticket )
      if ( predicate ) {
        builder.and( predicate )
      }
      if ( StringUtils.isNotBlank( employee ) ) {
        log.debug( "empleado: ${employee}" )
        builder.and( qNotaVenta.idEmpleado.eq( employee ) )
      }
      if ( builder.args?.any() ) {
        builder.and( qNotaVenta.factura.isNotEmpty() )
        List<NotaVenta> results = notaVentaRepository.findAll( builder, qNotaVenta.fechaHoraFactura.desc() ) as List<NotaVenta>
        if( results.size() <= 0 && factura.length() > 0){
          try{
            results = notaVentaRepository.findByFactura( String.format("%06d", NumberFormat.getInstance().parse(factura.trim())) )
          } catch (Exception e){
            println e
          }
        }
        return results?.any() ? results : [ ]
      }
    } else {
      log.warn( "no se realiza busqueda, parametros invalidos" )
    }
    return [ ]
  }

  @Override
  NotaVenta obtenerNotaVentaPorTicket( String ticket ) {
    log.info( "obteniendo notaVenta con ticket: ${ticket}" )
    Predicate predicate = generarPredicadoTicket( ticket )
    if ( predicate ) {
      OrderSpecifier orderSpecifier = QNotaVenta.notaVenta.fechaHoraFactura.desc()
      List<NotaVenta> resultados = notaVentaRepository.findAll( predicate, orderSpecifier ) as List<NotaVenta>
      NotaVenta notaVenta = resultados?.any() ? resultados.first() : null
      log.debug( "obtiene notaVenta id: ${notaVenta?.id}" )
      return notaVenta
    } else {
      log.warn( 'no se obtiene notaVenta, parametros invalidos' )
    }
    return null
  }

  void eliminarNotaVenta( String pOrderNbr ) {
    log.debug( String.format( "Eliminar Nota Venta: %s", pOrderNbr ) )
    EliminarNotaVentaTask task = new EliminarNotaVentaTask()
    NotaVenta order = notaVentaRepository.findOne( pOrderNbr )
    if ( order != null ) {
      task.addNotaVenta( order.id )
      log.debug( task.toString() )
      task.run()
    } else {
      log.debug( String.format( 'No existe Nota Venta: %s', pOrderNbr ) )
    }
  }

  SalesWithNoInventory obtenerConfigParaVentasSinInventario( ) {
    SalesWithNoInventory autorizacion = Registry.configForSalesWithNoInventory
    return autorizacion
  }

  Empleado obtenerEmpleadoDeNotaVenta( pOrderId ) {
    Empleado employee = null
    if ( StringUtils.trimToNull( pOrderId ) != null ) {
      NotaVenta order = notaVentaRepository.findOne( StringUtils.trimToEmpty( pOrderId ) )
      if ( ( order != null ) && ( StringUtils.trimToNull( order.idEmpleado ) != null ) ) {
        employee = RepositoryFactory.employeeCatalog.findOne( StringUtils.trimToEmpty( order.idEmpleado ) )
      }
    }
    return employee
  }

  @Transactional
  void saveOrder( NotaVenta pNotaVenta ) {
    if ( pNotaVenta != null ) {

      notaVentaRepository.saveAndFlush( pNotaVenta )
    }
  }

    @Override
    @Transactional
  NotaVenta obtenerSiguienteNotaVenta( Integer pIdCustomer ) {
    Date fechaStart = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH )
    Date fechaEnd = new Date( DateUtils.ceiling( new Date(), Calendar.DAY_OF_MONTH ).getTime() - 1 )
    QNotaVenta nota = QNotaVenta.notaVenta
    //List<NotaVenta> orders = notaVentaRepository.findByIdCliente( pIdCustomer )
    List<NotaVenta> orders = notaVentaRepository.findAll(nota.idCliente.eq(pIdCustomer).
            and(nota.fechaHoraFactura.between(fechaStart,fechaEnd)))
    NotaVenta order = null
    for (NotaVenta o : orders) {
      if ( o.detalles.size() > 0 && StringUtils.isBlank( o.factura )) {
        order = o
        break
      }
    }
    if (order == null) {
      ServiceFactory.customers.eliminarClienteProceso( pIdCustomer )
    }
    return order
  }


  @Override
  @Transactional
  void validaSurtePorGenericoInventariable( NotaVenta notaVenta ){
    List<DetalleNotaVenta> detalles = detalleNotaVentaRepository.findByIdFactura( notaVenta.id )
    for(DetalleNotaVenta det : detalles){
        if(!TAG_GENERICOS_INVENTARIABLES.contains(det.articulo.idGenerico)){
            det.surte = ' '
            detalleNotaVentaRepository.save( det )
            detalleNotaVentaRepository.flush()
        }
    }
  }


  @Override
  @Transactional
  void registraImpuestoPorFactura( NotaVenta notaVenta ){
    Parametro parametro = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.value )
    FacturasImpuestos impuesto = new FacturasImpuestos()
    impuesto.idFactura = notaVenta?.id
    impuesto.idImpuesto = parametro.valor
    impuesto.idSucursal = notaVenta.idSucursal
    impuesto.fecha = new Date()

    impuesto = facturasImpuestosRepository.save( impuesto )
    log.debug( "guardando idImpuesto ${impuesto.idImpuesto} a factura: ${impuesto.idFactura}" )
    facturasImpuestosRepository.flush()
  }



  @Override
  Boolean ticketReusoValido( String ticket, Integer idArticulo ){
    QNotaVenta nv = QNotaVenta.notaVenta
    NotaVenta nota = notaVentaRepository.findOne( nv.factura.eq(ticket.trim()).and(nv.sFactura.eq(TAG_NOTA_CANCELADA)) )
    Articulo articulo = articuloRepository.findOne( idArticulo )
    Boolean validTicket = false
    Boolean validPorDev = false
    Boolean valid
    if( nota != null && articulo != null ){
      for(DetalleNotaVenta det : nota.detalles){
        if(det.idArticulo == articulo.id){
          validTicket = true
        }
      }
      for(Pago pago : nota.pagos){
        if(pago.porDevolver.compareTo(BigDecimal.ZERO) > 0){
          validPorDev = true
        }
      }
    }
    if( validTicket && validPorDev ){
      valid = true
    }
    return valid
  }



  @Override
  Boolean montoValidoFacturacion( String ticketComp ){
    log.debug( "montoValidoFacturacion( )" )
    Boolean esValido = true
    BigDecimal montoTotal = BigDecimal.ZERO
    BigDecimal montoCupones = BigDecimal.ZERO
    String[] ticketTmp = ticketComp.split("-")
    String ticket = ''
    if(ticketTmp.length >= 2){
      ticket = ticketTmp[1]
    }
    QNotaVenta nv = QNotaVenta.notaVenta
    NotaVenta nota = notaVentaRepository.findOne( nv.factura.eq(ticket.trim()) )
    if(nota != null){
      for(Pago pago : nota.pagos){
        montoTotal = montoTotal.add(pago.monto)
        if(pago.idFPago.trim().startsWith(TAG_PAGO_CUPON)){
          montoCupones = montoCupones.add(pago.monto)
        }
      }
      if(montoTotal.subtract(montoCupones) == 0){
        esValido = false
      }
    }
    return esValido
  }


  @Override
  List<NotaVenta> obtenerDevolucionesPendientes( Date fecha ) {
      log.info( "obteniendo pagos del dia: ${fecha}" )
      Date fechaInicio = DateUtils.truncate( fecha, Calendar.DAY_OF_MONTH );
      Date fechaFin = new Date( DateUtils.ceiling( fecha, Calendar.DAY_OF_MONTH ).getTime() - 1 );
      List<NotaVenta> lstNotasVentas = new ArrayList<NotaVenta>()
      QModificacion mod = QModificacion.modificacion
      List<Modificacion> lstModificaciones = modificacionRepository.findAll( mod.fecha.between(fechaInicio, fechaFin).
              and(mod.tipo.equalsIgnoreCase('can')))
      for(Modificacion modificacion : lstModificaciones){
          NotaVenta notaVenta = notaVentaRepository.findOne( modificacion.idFactura )
          if(notaVenta != null){
              Boolean pendiente = false
              for(Pago pago : notaVenta.pagos){
                  if( pago.porDevolver.compareTo(BigDecimal.ZERO) > 0){
                      pendiente = true
                  }
              }
              if(pendiente){
                  lstNotasVentas.add(notaVenta)
              }
          }
      }
      return lstNotasVentas
  }


  @Override
  NotaVenta buscarNotasReuso( String idFactura ) {
    log.debug( "buscarNotasReuso( )" )
    NotaVenta nota = new NotaVenta()
    NotaVenta notas = notaVentaRepository.findOne( idFactura )
    QPago pay = QPago.pago
    List<Pago> lstPagos = pagoRepository.findAll( pay.referenciaPago.eq(notas.id.trim()) )
    if( lstPagos.size() > 0 ){
      NotaVenta notaTmp = lstPagos.first().notaVenta
      for(DetalleNotaVenta det : notaTmp.detalles){
        if(TAG_REUSO.equalsIgnoreCase(det.surte.trim())){
          nota = notaTmp
        }
      }
    }
    return nota != null && nota.id != null ? nota : null
  }


  @Override
  NotaVenta obtenerNotaVentaOrigen( String idNotaVenta ){
    NotaVenta nota = notaVentaRepository.findOne( idNotaVenta )
    NotaVenta notaOrigen = null
    String idNotaOrigen = ''
    for(Pago payment : nota.pagos){
      if(TAG_TRANSFERENCIA.equalsIgnoreCase(payment.idFPago) && payment?.referenciaPago?.trim().length() > 0){
        idNotaOrigen = payment?.referenciaPago?.trim()
        notaOrigen = notaVentaRepository.findOne( idNotaOrigen )
      }
    }
    return notaOrigen
  }


  @Override
  Boolean validaSoloInventariables( String idFactura ) {
    log.debug( "validaSoloInventariables( )" )
    NotaVenta nota = notaVentaRepository.findOne( idFactura )
    Boolean esInventariable = true
    for(DetalleNotaVenta det : nota.detalles){
      if( TAG_GENERICOS_B.contains(det?.articulo?.idGenerico?.trim()) ){
        esInventariable = false
      }
    }
    return esInventariable
  }


  @Override
  void insertaJbAnticipoInventariables( String idFactura ){
    log.debug( "insertaJbAnticipoInventariables( )" )
    NotaVenta nota = notaVentaRepository.findOne( idFactura )
    if( nota != null ){
      Jb jb = new Jb()
      String factura = nota.factura.replaceFirst("^0*", "")
      jb.rx = factura
      jb.estado = 'RTN'
      jb.id_cliente = nota.idCliente.toString().trim()
      jb.emp_atendio = nota.idEmpleado
      jb.num_llamada = 0
      jb.saldo = nota.ventaNeta.subtract( nota.sumaPagos )
      jb.fecha_promesa = nota.fechaPrometida
      jb.jb_tipo = 'REF'
      jb.id_mod = '0'
      jb.fecha_mod = new Date()
      jb.cliente = nota?.cliente?.nombreCompleto
      jb.fecha_venta = nota?.fechaHoraFactura
      jb = jbRepository.saveAndFlush( jb )

      JbTrack jbTrack = new JbTrack()
      jbTrack.rx = jb.rx
      jbTrack.estado = 'RTN'
      jbTrack.obs = 'TRABAJO CON SALDO'
      jbTrack.emp = jb.emp_atendio
      jbTrack.fecha = new Date()
      jbTrack.id_mod = '0'
      jbTrackRepository.saveAndFlush( jbTrack )
    }
  }

  @Override
  void correScriptRespaldoNotas( String idFactura ){
    log.debug( "correScriptRespaldoNotas( )" )
    NotaVenta nota = notaVentaRepository.findOne( idFactura )
    if( nota != null ){
      try{
      String cmd = String.format( "%s %s", Registry.commandBakpOrder, nota.id);
      Process p = Runtime.getRuntime().exec(cmd);
      log.debug( "comando a ejecutar <${cmd}>" )
      } catch (Exception e){
        println e
      }
    }
  }



}