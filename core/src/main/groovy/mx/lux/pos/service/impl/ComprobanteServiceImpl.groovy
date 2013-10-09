package mx.lux.pos.service.impl

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.util.logging.Slf4j
import mx.lux.pos.service.ComprobanteService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.math.MathContext
import javax.annotation.Resource

import mx.lux.pos.model.*
import mx.lux.pos.repository.*
import mx.lux.pos.service.business.Registry

import java.math.RoundingMode

@Slf4j
@Service( 'comprobanteService' )
@Transactional( readOnly = true )
class ComprobanteServiceImpl implements ComprobanteService {

  private static final String DATE_TIME_FORMAT = 'dd-MM-yyyy HH:mm:ss'
  private static final String TAG_GENERICO_A = 'A'
  private static final String TAG_GENERICO_B = 'B'
  private static final String TAG_GENERICO_C = 'C'
  private static final String TAG_GENERICO_H = 'H'
  private static final String TAG_GENERICO_E = 'E'

  @Resource
  private ComprobanteRepository comprobanteRepository

  @Resource
  private DetalleComprobanteRepository detalleComprobanteRepository

  @Resource
  private NotaVentaRepository notaVentaRepository

  @Resource
  private ImpuestoRepository impuestoRepository

  @Resource
  private DetalleNotaVentaRepository detalleNotaVentaRepository

  @Resource
  private ArticuloRepository articuloRepository

  @Resource
  private PagoRepository pagoRepository

  @Resource
  private SucursalRepository sucursalRepository

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private ReimpresionRepository reimpresionRepository

  private static Integer maxLength

  @Override
  Comprobante obtenerComprobante( String idFiscal ) {
    log.info( "obteniendo comprobante idFiscal: ${idFiscal}" )
    if ( StringUtils.isNotBlank( idFiscal ) ) {
      Comprobante comprobante = comprobanteRepository.findByIdFiscal( idFiscal )
      log.debug( "obtiene comprobante idFiscal: ${comprobante?.idFiscal}," )
      log.debug( "fechaImpresion: ${comprobante?.fechaImpresion?.format( DATE_TIME_FORMAT )}" )
      return comprobante
    } else {
      log.warn( 'no se obtiene comprobante, parametro invalido' )
    }
    return null
  }

  private Integer getMaxLength( ) {
    if (maxLength == null) {
      maxLength = Registry.maxLengthDescription
    }
    return maxLength
  }

  private boolean esTicketValido( String ticket ) {
    log.info( "validando estructura ticket: ${ticket}" )
    List<String> tokens = StringUtils.splitPreserveAllTokens( ticket, '-' )
    if ( StringUtils.isNotBlank( ticket ) && tokens?.size() == 2 ) {
      String centroCostos = tokens.get( 0 )
      String factura = tokens.get( 1 )
      if ( StringUtils.isNotBlank( factura ) && StringUtils.isNotBlank( centroCostos ) ) {
        log.debug( 'estructura ticket valida' )
        return true
      } else {
        log.warn( 'elementos ticket invalidos, ticket invalido' )
      }
    } else {
      log.warn( 'no se valida ticket, parametro invalido' )
    }
    return false
  }

  @Override
  List<Comprobante> listarComprobantesPorTicket( String ticket ) {
    log.info( "listando comprobantes por ticket: ${ticket}" )
    if ( esTicketValido( ticket ) ) {
      List<Comprobante> comprobantes = comprobanteRepository.findByTicketOrderByFechaImpresionDesc( ticket )
      log.debug( "obtiene comprobantes idFiscal: ${comprobantes*.idFiscal}" )
      return comprobantes?.any() ? comprobantes : [ ]
    } else {
      log.warn( 'no se listan comprobantes, parametro invalido' )
    }
    return [ ]
  }

  private String generarParametrosServicioWeb( Comprobante comprobante, List<DetalleComprobante> detalles ) {
    log.info( "generando parametros servicio web facturacion, ticket: ${comprobante?.ticket}" )
    if ( esTicketValido( comprobante?.ticket ) ) {
      if ( detalles?.any() ) {
        Integer idSucursal = sucursalRepository.getCurrentSucursalId()
        Parametro parametroEmpresa = parametroRepository.findOne( TipoParametro.EMP_ELECTRONICO.value )
        Parametro parametroTasa = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.value )
        Impuesto iva = impuestoRepository.findOne( parametroTasa.valor )
        String plantilla = '$id_sucursal|$ticket|$tipo_comprobante|$forma_pago|$subtotal|$total|' +
            '$metodo_pago|$impuestos|$tasa|$rfc|$nombre|$calle|$colonia|$municipio|$estado|$pais|' +
            '$codigo_postal|$email|$observaciones|$empresa|$tipo|<% listaDetalles.each { print it } %>'
        String decimalFormat = '%,3.2f'
        Boolean esAnteojoGraduado = false
        List<String> listaDetallesTmp = [ ]
        List<String> listaDetalles = [ ]
        detalles.eachWithIndex { DetalleComprobante tmp, int idx ->
          String cantidad = tmp.cantidad ?: ''
          String articulo = tmp.articulo ?: ''
          String desc = StringUtils.trimToEmpty( tmp.descripcion )
          String descripcion = ''

          if( tmp.descripcion.contains('ANTEOJO GRADUADO') ){
            esAnteojoGraduado = true
          }

          if ( desc.length() > 0 ) {
            if ( desc.length() >= this.getMaxLength() ) {
              descripcion = desc.substring( 0, this.getMaxLength() )
            } else {
              descripcion = desc
            }
          }

          String precio = sprintf( decimalFormat, tmp?.precioUnitario ?: BigDecimal.ZERO )
          String importe = sprintf( decimalFormat, tmp?.importe ?: BigDecimal.ZERO )
          listaDetallesTmp.add( "${idx == 0 ? '>' : ''}${cantidad}|${articulo}|${descripcion}|${precio}|${importe}|>" )
        }
        if( esAnteojoGraduado ){
          for(String det : listaDetallesTmp){
              if( det.contains('ANTEOJO GRADUADO') ){
                listaDetalles.add( det )
              }
          }
        } else {
          listaDetalles.addAll( listaDetallesTmp )
        }

        Map<String, String> valores = [
            id_sucursal: idSucursal ?: '',
            ticket: comprobante.ticket,
            tipo_comprobante: 'ingreso',
            forma_pago: comprobante.formaPago ?: '',
            subtotal: sprintf( decimalFormat, comprobante.subtotal ?: BigDecimal.ZERO ),
            total: comprobante.importe?.replace( '$', '' ) ?: '0.00',
            metodo_pago: comprobante.metodoPago ?: '',
            impuestos: sprintf( decimalFormat, comprobante.impuestos ?: BigDecimal.ZERO ),
            tasa: iva.tasa ?: '',
            rfc: comprobante.rfc ?: '',
            nombre: comprobante.razon ?: '',
            calle: comprobante.calle ?: '',
            colonia: comprobante.colonia ?: '',
            municipio: comprobante.municipio ?: '',
            estado: comprobante.estado ?: '',
            pais: comprobante.pais ?: '',
            codigo_postal: comprobante.codigoPostal ?: '',
            email: comprobante.email ?: '',
            observaciones: comprobante.observaciones ?: '',
            empresa: parametroEmpresa?.valor ?: '',
            tipo: comprobante.idOrigen ?: 'N',
            listaDetalles: listaDetalles
        ] as Map
        Template template = new SimpleTemplateEngine().createTemplate( plantilla )
        String parametros = template.make( valores ).toString()
        log.debug( "parametros generados: ${parametros}" )
        return parametros
      } else {
        log.warn( 'no se generan parametros servicio web facturacion, no se obtienen detalles' )
      }
    } else {
      log.warn( 'no se generan parametros servicio web facturacion, parametro invalido' )
    }
    return null
  }

  private Comprobante procesarRespuestaServicioWeb( Comprobante comprobante, String respuesta ) {
    log.info( 'procesando respuesta servicio web facturacion' )
    if ( StringUtils.isNotBlank( respuesta ) ) {
      String resultado = respuesta?.find( /<XX>\s*(.*)\s*<\/XX>/ ) {m, r -> return r}
      log.debug( "resultado solicitud: ${resultado}" )
      List<String> elementos = resultado?.tokenize( '|' )
      if ( elementos?.any() ) {
        String codigo = elementos.first()
        log.debug( "codigo resultado: ${codigo}" )
        if ( codigo.matches( /.+PROCESADA.+/ ) ) {
          comprobante.idFiscal = elementos.get( 1 )
          comprobante.url = elementos.get( 2 )
          comprobante.xml = elementos.get( 3 )
          return comprobante
        } else {
          log.warn( 'error al procesar la solicitud' )
        }
      } else {
        log.warn( 'no se recibe respuesta de servicio web facturacion' )
      }
    } else {
      log.warn( 'no se solicita servicio web facturacion, parametro invalido' )
    }
    return null
  }

  private Comprobante solicitarComprobanteServicioWeb( Comprobante comprobante, List<DetalleComprobante> detalles ) {
    log.info( "solicitando comprobante fiscal a servicio web facturacion, ticket: ${comprobante?.ticket}" )
    if ( StringUtils.isNotBlank( comprobante?.ticket ) && detalles?.any() ) {
      Parametro parametroURL = parametroRepository.findOne( TipoParametro.PIDE_FACTURA.value )
      log.debug( "url obtenida de servicio web: ${parametroURL?.valor}" )
      if ( StringUtils.isNotBlank( parametroURL?.valor ) ) {
        String parametros = generarParametrosServicioWeb( comprobante, detalles )
        if ( StringUtils.isNotBlank( parametros ) ) {
          URL url = "${parametroURL.valor}?arg=${URLEncoder.encode( parametros ?: '' )}".toURL()
          log.debug( "url a solicitar: ${url}" )
          Comprobante resultado = procesarRespuestaServicioWeb( comprobante, url?.text )
          log.debug( "comprobante procesado, idFiscal: ${resultado?.idFiscal}" )
          return resultado
        } else {
          log.warn( 'no se solicita servicio web facturacion, parametros no generados' )
        }
      } else {
        log.warn( 'no se solicita servicio web facturacion, url servicio invalida' )
      }
    } else {
      log.warn( 'no se solicita servicio web facturacion, parametros invalidos' )
    }
    return null
  }

  @Override
  @Transactional
  Comprobante registrarComprobante( Comprobante comprobante ) {
    log.info( "registrando comprobante fiscal, ticket: ${comprobante?.ticket}, idFactura: ${comprobante?.idFactura}" )
    if ( esTicketValido( comprobante?.ticket ) && StringUtils.isNotBlank( comprobante?.idFactura ) ) {
      NotaVenta venta = notaVentaRepository.findOne( comprobante.idFactura )
      log.debug( "obtiene notaVenta id: ${venta?.id}" )
      if ( StringUtils.isNotBlank( venta?.id ) ) {
        try {
          Parametro parametroTasa = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.value )
          String idIva = parametroTasa?.valor
          Impuesto iva = impuestoRepository.findOne( idIva )
          String tasa = '0'
          if( iva != null ){
            tasa = iva.tasa.toString().trim()
          }
          log.debug( "obtiene tasa vigente: ${tasa}" )
          Double referencia = ( ( tasa?.isDouble() ? tasa.toDouble() : 0 ) / 100 ) + 1
          Double referenciaAB = ( ( tasa?.isDouble() ? tasa.toDouble() : 0 ) / 100 )
          MathContext mathContext = new MathContext( 5 )
          BigDecimal montoCupones = BigDecimal.ZERO
          for(Pago pago : venta.pagos){
            if( pago.idFPago.trim().startsWith('C')){
              montoCupones = montoCupones.add(pago.monto)
            }
          }
          BigDecimal total = venta.ventaNeta ? venta.ventaNeta.subtract(montoCupones): BigDecimal.ZERO
          BigDecimal subTotal = total.divide( referencia, 10, RoundingMode.CEILING ) ?: BigDecimal.ZERO
          BigDecimal impuestos = total.subtract( subTotal )

          Comprobante ultimo = null
          List<Comprobante> anteriores = comprobanteRepository.findByTicketOrderByFechaImpresionDesc( comprobante.ticket )
          log.debug( "obtiene comprobantes anteriores idFiscal: ${anteriores*.idFiscal}" )
          if ( anteriores?.any() ) {
            ultimo = anteriores.first()
            log.debug( "obtiene ultimo comprobante: ${ultimo?.idFiscal}" )
            ultimo.estatus = 'R'
            comprobante.idOrigen = ultimo.idFiscal
            comprobante.tipo = 'R'
          } else {
            comprobante.tipo = 'O'
          }

          List<Pago> pagosVenta = pagoRepository.findByIdFacturaOrderByFechaAsc( venta.id )
          log.debug( "obtiene pagos notaVenta: ${pagosVenta*.id}" )
          pagosVenta?.removeAll { Pago pmt ->
            String idFormaPago = pmt?.idFormaPago ?: ''
            'VA'.equalsIgnoreCase( idFormaPago ) || 'TR'.equalsIgnoreCase( idFormaPago )
          }
          Pago pago = new Pago()
          for(Pago payment : pagosVenta){
            if(!payment.idFPago.startsWith('C')){
              pago = payment
              break
            }
          }

          List<DetalleComprobante> detalles = [ ]
          List<DetalleNotaVenta> detallesVenta = detalleNotaVentaRepository.findByIdFacturaOrderByIdArticuloAsc( venta.id )
          log.debug( "obtiene detalles notaVenta: ${detallesVenta*.id}" )
          Boolean genericoAyB = false
          BigDecimal precioUnidad = BigDecimal.ZERO
          Boolean genericoA = false
          Boolean genericoB = false
          for(DetalleNotaVenta det : detallesVenta){
            if(det.articulo.idGenerico.equalsIgnoreCase(TAG_GENERICO_A)){
                genericoA = true
                precioUnidad = precioUnidad.add(det.precioUnitFinal)
            } else if(det.articulo.idGenerico.equalsIgnoreCase(TAG_GENERICO_B)){
                genericoB = true
                precioUnidad = precioUnidad.add(det.precioUnitFinal)
            }

            if(genericoA && genericoB){
              genericoAyB = true
            }
          }
          detallesVenta?.each { DetalleNotaVenta det ->
            Articulo articulo = articuloRepository.findOne( det?.idArticulo ?: 0 )
                if ( articulo?.id ) {
                    Integer cantidad = det?.cantidadFac ?: 0
                    BigDecimal precioVenta = det.precioUnitFinal ?: BigDecimal.ZERO
                    BigDecimal precioUnitario = precioVenta.divide( referencia, 10, RoundingMode.CEILING ) ?: BigDecimal.ZERO
                    BigDecimal precioUnitarioAB = det.notaVenta.ventaNeta.divide( referenciaAB, mathContext ) ?: BigDecimal.ZERO
                    BigDecimal importe = precioUnitario.multiply( cantidad )
                    BigDecimal importeAB = precioUnitarioAB.multiply( cantidad )
                    Boolean ABinsertado = false
                    String idArticulo = articulo.id
                    String article = articulo.articulo
                    String color = articulo.codigoColor
                    String idGenerico = articulo.idGenerico
                    String descripcion = ""
                    Double quantity = cantidad
                    BigDecimal priceUnit = precioUnitario
                    BigDecimal amount = importe
                    println genericoAyB
                    if(genericoAyB && articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_B)){
                        idArticulo = 'ANTEOJO'
                        article = ''
                        idGenerico = 'ANTEOJO'
                        descripcion = 'ANTEOJO GRADUADO'
                        quantity = cantidad
                        priceUnit = total
                        amount = total
                    } else if(!genericoAyB && articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_A)){
                        descripcion = 'ARMAZON'
                    } else if(!genericoAyB && articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_B)){
                        descripcion = 'LENTE GRADUADO'
                    } else if(articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_C) || articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_H)){
                        descripcion = 'LENTES DE CONTACTO'
                    } else if(articulo.idGenerico.trim().equalsIgnoreCase(TAG_GENERICO_E)){
                        descripcion = 'ACCESORIOS VARIOS'
                    }

                    DetalleComprobante detalle = new DetalleComprobante(
                            idArticulo: idArticulo,
                            articulo: article,
                            color: color,
                            idGenerico: idGenerico,
                            descripcion: descripcion,
                            cantidad: quantity,
                            precioUnitario: priceUnit,
                            importe: amount
                    )
                    if( genericoAyB && descripcion.trim().contains('ANTEOJO GRADUADO') ){
                      if( !ABinsertado ){
                        log.debug( "genera detalle comprobante: ${detalle.dump()}" )
                        detalles.add( detalle )
                      }
                    } else if( !genericoAyB || (genericoAyB && (!idGenerico.equalsIgnoreCase(TAG_GENERICO_A) && !idGenerico.equalsIgnoreCase(TAG_GENERICO_B))) ) {
                      log.debug( "genera detalle comprobante: ${detalle.dump()}" )
                      detalles.add( detalle )
                    }
                    if(descripcion.trim().contains('ANTEOJO GRADUADO')){
                      ABinsertado = true
                    }
                }
          }

          List<Pago> lstPagos = venta.pagos as List<Pago>
          String formaPago = ''
          Collections.sort(lstPagos, new Comparator<Pago>() {
              @Override
              int compare(Pago o1, Pago o2) {
                  return o1.parcialidad.compareTo(o2.parcialidad)
              }
          })

          for(Pago payment : lstPagos){
            if(payment?.parcialidad.length() > 0){
              formaPago = "PARCIALIDAD ${payment.parcialidad} DE ${payment.parcialidad}"
            } else {
              formaPago = 'UNA SOLA EXHIBICION'
            }
          }

          comprobante.factura = venta.factura
          comprobante.idCliente = venta.idCliente
          comprobante.importe = sprintf( '$%,3.2f', total ?: BigDecimal.ZERO )
          comprobante.subtotal = subTotal
          comprobante.impuestos = impuestos
          comprobante.estatus = 'N'
          comprobante.formaPago = formaPago
          comprobante.metodoPago = "${pago?.eTipoPago?.descripcion ?: ''} ${pago?.referenciaPago ?: ''}"

          log.debug( "genera comprobante ${comprobante.dump()}" )

          //if(comprobante.metodoPago.trim().length() > 0){
            comprobante = solicitarComprobanteServicioWeb( comprobante, detalles )
          //}
          if ( StringUtils.isNotBlank( comprobante?.idFiscal ) && comprobante.metodoPago.trim().length() > 0) {
            comprobante = comprobanteRepository.save( comprobante )
            if ( comprobante?.id ) {
              detalles.each { DetalleComprobante detalle ->
                detalle.idFiscal = comprobante.idFiscal
              }
              detalleComprobanteRepository.save( detalles )
              if ( ultimo?.id ) {
                comprobanteRepository.save( ultimo )
                Reimpresion reimpresion = new Reimpresion(
                    nota: 'Fa',
                    idNota: venta.id,
                    idEmpleado: comprobante.idEmpleado,
                    factura: comprobante.idFiscal
                )
                reimpresionRepository.save( reimpresion )
              }
              log.debug( "comprobante registrado con idFiscal: ${comprobante?.idFiscal}" )
              return comprobante
            } else {
              throw new Exception( 'error al guardar comprobante' )
            }
          } else {
            log.error( '[ComprobanteServiceImpl]registrarComprobante: no se obtiene idFiscal valido' )
          }
        } catch ( ex ) {
          log.error( 'no se registra comprobante fiscal, ocurrio un error', ex )
        }
      } else {
        log.warn( 'no se registra comprobante fiscal, no existe notaVenta' )
      }
    } else {
      log.warn( 'no se registra comprobante fiscal, parametros invalidos' )
    }
    return null
  }

  private File descargarArchivo( URL url, File archivo ) {
    log.info( "descargando archivo de: ${url} a: ${archivo?.path}" )
    if ( url && StringUtils.isNotBlank( archivo?.path ) ) {
      if ( archivo?.parentFile?.canWrite() ) {
        try {
          archivo.exists() ? archivo.delete() : null
          url.withInputStream { BufferedInputStream is ->
            archivo.withOutputStream { BufferedOutputStream os ->
              os << is
            }
          }
          log.debug( "escribe archivo ${archivo.path} de ${archivo.size()} bytes" )
          return archivo
        } catch ( ex ) {
          log.error( 'error al escribir archivo', ex )
        }
      } else {
        log.warn( 'no se puede descargar archivo, no se puede escribir en ruta' )
      }
    } else {
      log.warn( 'no se puede descargar archivo, parametros invalidos' )
    }
    return null
  }

  @Override
  List<File> descargarArchivosComprobante( String idFiscal ) {
    log.info( "descargando archivos comprobante con idFiscal: ${idFiscal}" )
    if ( StringUtils.isNotBlank( idFiscal ) ) {
      Comprobante comprobante = obtenerComprobante( idFiscal )
      if ( comprobante?.id ) {
        Parametro parametroRuta = parametroRepository.findOne( TipoParametro.RUTA_COMPROBANTES.value )
        try {
          File dirFacturas = new File( parametroRuta?.valor )
          dirFacturas.exists() ?: dirFacturas.mkdir()
          File dirReceptor = new File( dirFacturas, comprobante.rfc )
          dirReceptor.exists() ?: dirReceptor.mkdir()
          String nombreDirComprobante = "${comprobante.idFiscal}-${comprobante.ticket}"
          File dirComprobante = new File( dirReceptor, nombreDirComprobante )
          dirComprobante.exists() ?: dirComprobante.mkdir()
          File xmlComprobante = new File( dirComprobante, "${comprobante.ticket}.xml" )
          xmlComprobante.exists() ?: descargarArchivo( comprobante.xml?.toURL(), xmlComprobante )
          File pdfComprobante = new File( dirComprobante, "${comprobante.ticket}.pdf" )
          pdfComprobante.exists() ?: descargarArchivo( comprobante.url?.toURL(), pdfComprobante )
          return [ xmlComprobante, pdfComprobante ]
        } catch ( ex ) {
          log.error( 'error al descargar archivos', ex )
        }
      } else {
        log.warn( 'no se descargan archivos comprobante, comprobante no existe' )
      }
    } else {
      log.warn( 'no se descargan archivos comprobante, parametro invalido' )
    }
    return [ ]
  }
}
