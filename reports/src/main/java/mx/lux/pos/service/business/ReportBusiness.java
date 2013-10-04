package mx.lux.pos.service.business;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.OrderSpecifier;
import mx.lux.pos.model.*;
import mx.lux.pos.repository.*;
import mx.lux.pos.service.impl.ReportServiceImpl;
import net.sf.jasperreports.engine.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

@Component
public class ReportBusiness {

    private static Logger log = LoggerFactory.getLogger( ReportServiceImpl.class );

    @Resource
    private ImpuestoRepository impuestoRepository;

    @Resource
    private ExamenRepository examenRepository;

    @Resource
    private TipoPagoRepository tipoPagoRepository;

    @Resource
    private BancoEmisorRepository bancoEmisorRepository;

    @Resource
    private ClienteRepository clienteRepository;

    @Resource
    private CotizacionRepository cotizacionRepository;

    @Resource
    private DescuentoRepository descuentoRepository;

    @Resource
    private PromocionRepository promocionRepository;

    @Resource
    private TrabajoRepository trabajoRepository;

    @Resource
    private TrabajoTrackRepository trabajoTrackRepository;

    @Resource
    private ArticuloRepository articuloRepository;

    @Resource
    private ParametroRepository parametroRepository;

    @Resource
    private RecetaRepository recetaRepository;

    @Resource
    private PagoRepository pagoRepository;

    @Resource
    private DevolucionRepository devolucionRepository;

    @Resource
    private EmpleadoRepository empleadoRepository;

    @Resource
    private NotaVentaRepository notaVentaRepository;

    @Resource
    private ModificacionRepository modificacionRepository;

    @Resource
    private DetalleNotaVentaRepository detalleNotaVentaRepository;

    @Resource
    private TransInvRepository transInvRepository;

    @Resource
    private TransInvDetalleRepository transInvDetalleRepository;

    @Resource
    private SucursalRepository sucursalRepository;

    @Resource
    private OrdenPromDetRepository ordenPromDetRepository;

    @Resource
    private PrecioRepository precioRepository;

    private static final Integer TAG_PUESTO_OFTALMOLOGO = 3;
    private static final String TAG_CANCELADO = "T";
    private static final String TAG_TIPO_CANCELADO = "can";

    public List<IngresoPorDia> obtenerIngresoporDia( Date fechaInicio, Date fechaFin ) {
        log.info( "obtenerIngresoporDia()" );

        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = iva.getTasa();

        List<IngresoPorDia> lstIngresos = new ArrayList<IngresoPorDia>();
        log.info( "fechaInicio:{},  fechaFin:{}", fechaInicio, fechaFin );
        List<Pago> lstpagos = pagoRepository.findByFechaBetweenOrderByFechaAsc( fechaInicio, fechaFin );

        for ( Pago pago : lstpagos ) {
            if ( isPagoValid( pago ) && pago.getNotaVenta().getFactura() != null && pago.getNotaVenta().getFactura() != ""
                    && !TAG_CANCELADO.equalsIgnoreCase(pago.getNotaVenta().getsFactura()) ) {
                IngresoPorDia ingreso = FindOrCreate( lstIngresos, pago.getFecha() );
                ingreso.AcumulaMonto( pago.getMonto(), ivaTasa );
            }
        }

        List<Devolucion> lstDevoluciones = devolucionRepository.findByFechaBetween( fechaInicio, fechaFin );
        for ( Devolucion devolucion : lstDevoluciones ) {
            IngresoPorDia devoluciones = FindOrCreate( lstIngresos, devolucion.getFecha() );
            devoluciones.AcumulaDevolucion( devolucion.getMonto(), ivaTasa );
        }

        for(IngresoPorDia ingreso : lstIngresos){
            ingreso.setMontoAcumulado(ingreso.getMontoAcumulado().divide(new BigDecimal(1+(ivaTasa/100)), 10, RoundingMode.HALF_EVEN));
        }
        return lstIngresos;
    }

    protected boolean isPagoValid( Pago pPago ) {
        boolean valid = true;
        if ( valid )
            valid = !pPago.getIdFormaPago().equalsIgnoreCase( "BD" );
        if ( valid )
            valid = !pPago.getIdFormaPago().equalsIgnoreCase( "EX" );

        Parametro convenios = parametroRepository.findOne( TipoParametro.CONV_NOMINA.getValue() );
        if ( valid )
            valid = !pPago.getNotaVenta().getIdConvenio().contains( convenios.getValor() );

        return valid;
    }

    public IngresoPorDia FindOrCreate( List<IngresoPorDia> lstIngresos, Date fecha ) {
        Date onlyDay = DateUtils.truncate( fecha, Calendar.DATE );
        IngresoPorDia found = null;
        for ( IngresoPorDia ingresos : lstIngresos ) {
            if ( ingresos.getFecha().equals( onlyDay ) ) {

                found = ingresos;
                break;
            }
        }
        if ( found == null ) {
            found = new IngresoPorDia( onlyDay );
            lstIngresos.add( found );
        }
        return found;
    }

    public IngresoPorVendedor FindorCreate( List<IngresoPorVendedor> lstIngresos, String idEmpleado ) {
        IngresoPorVendedor found = null;
        for ( IngresoPorVendedor ingresos : lstIngresos ) {
            if ( ingresos.getIdEmpleado().equals( idEmpleado ) ) {
                found = ingresos;
                break;
            }
        }
        if ( found == null ) {
            found = new IngresoPorVendedor( idEmpleado );
            Empleado empleado = empleadoRepository.findOne( idEmpleado );
            if ( empleado != null ) {
                found.setNombre( empleado.nombreCompleto() );
                //found.setPagos(pagos)
            }
            lstIngresos.add( found );
        }
        return found;
    }

    public String CompilayGeneraReporte( org.springframework.core.io.Resource template, Map<String, Object> parametros, File report ) {

        try {
            /*Runtime rt = Runtime.getRuntime();
            long memoriaAntes=0, memoriaDespues=0, memoriaConsumida=0;
            System.gc();
            memoriaAntes = rt.freeMemory();*/

            String cmd = "chmod 777 "+report.getAbsolutePath();
            Process p = Runtime.getRuntime().exec(cmd);
            JasperReport jasperReport = JasperCompileManager.compileReport( template.getInputStream() );
            JasperPrint jasperPrint = JasperFillManager.fillReport( jasperReport, parametros, new JREmptyDataSource() );
            JasperExportManager.exportReportToHtmlFile( jasperPrint, report.getPath() );
            Desktop.getDesktop().open( report );
            log.info( "Mostrar Reporte" );

            Runtime garbage = Runtime.getRuntime();
            garbage.gc();
            /*memoriaDespues = rt.freeMemory();
            memoriaConsumida = memoriaAntes - memoriaDespues;
            System.out.println( String.format("Memoria Antes: %s", memoriaAntes) );
            System.out.println( String.format("Memoria Despues: %s", memoriaDespues) );
            System.out.println( String.format("Memoria Consumida: %s", memoriaConsumida) );*/

            return report.getPath();
        } catch ( JRException e ) {
            log.error( "error al compilar y generar reporte", e );
        } catch ( IOException e ) {
            log.error( "error al compilar y generar reporte", e );
        }
        return report.getPath();
    }

    public List<IngresoPorVendedor> obtenerVentasporVendedor( Date fechaInicio, Date fechaFin ) {

        List<IngresoPorVendedor> lstIngresos = new ArrayList<IngresoPorVendedor>();
        List<String> empleados = notaVentaRepository.empleadosFechas(fechaInicio,fechaFin);

        for(String empleado : empleados){
         Empleado emp = empleadoRepository.findById(empleado);
            if(emp.getIdPuesto() == 1 || emp.getIdPuesto() == 5){

        QNotaVenta notaVenta = QNotaVenta.notaVenta;
        List<NotaVenta> lstVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( notaVenta.factura.isNotEmpty().and( notaVenta.factura.isNotNull() ).
                and( notaVenta.fechaHoraFactura.between( fechaInicio, fechaFin ) ).and(notaVenta.sFactura.ne("T")).and(notaVenta.idEmpleado.eq(empleado.trim())),
                notaVenta.idEmpleado.asc(), notaVenta.fechaHoraFactura.asc() );

        IngresoPorVendedor ingreso = agregaRegistros(lstVentas);


        lstIngresos.add(ingreso);
            }
        }


        return lstIngresos;
    }

    private IngresoPorVendedor agregaRegistros(List<NotaVenta> lstVentas){
        List<IngresoPorFactura> ingresoPorFacturas = new ArrayList<IngresoPorFactura>();
        NotaVenta nVenta = new NotaVenta();
        for ( NotaVenta venta : lstVentas ) {
            if ( venta.getFactura() != null ) {
                String idEmpleado = venta.getIdEmpleado();
                String articulos = new String();

                for(DetalleNotaVenta detalle : venta.getDetalles()){

                    if( detalle.getArticulo() != null){

                        articulos =  detalle.getArticulo().getArticulo()  + "," + articulos;
                    }


                }

                IngresoPorFactura ingresoPorFactura = new IngresoPorFactura(venta.getFactura());
                ingresoPorFactura.setTotal(venta.getVentaNeta());
                ingresoPorFactura.setFechaPago(venta.getFechaHoraFactura());
                ingresoPorFactura.setDescripcion(articulos);

                List<Pago> pagos = pagoRepository.findByIdFactura(venta.getId());
                BigDecimal cupon = new BigDecimal(0);
                for(Pago pago : pagos){
                    if(pago.getIdFPago() != null){
                        if(pago.getIdFPago().trim().equals("C1") ||pago.getIdFPago().trim().equals("C2") || pago.getIdFPago().trim().equals("C3") || pago.getIdFPago().trim().equals("C4") ){
                            cupon = pago.getMonto();
                        }
                    }
                   
                }

                String cuponString = "";

               if(cupon != null){
                   System.out.println(cupon.equals( new BigDecimal(0)));
                if(cupon.equals( new BigDecimal(0))){
                    cuponString = "-";
                }  else{
                    cuponString = cupon.toString();
                }

               }

                ingresoPorFactura.setSumaMonto(venta.getVentaTotal().subtract(cupon));



                ingresoPorFactura.setColor(cuponString);

                ingresoPorFacturas.add(ingresoPorFactura);



            }
            nVenta=venta;
        }




        IngresoPorVendedor ingreso = new  IngresoPorVendedor();
        ingreso.setIdEmpleado(nVenta.getIdEmpleado());
        ingreso.setNombre(nVenta.getEmpleado().getNombreCompleto());
        ingreso.setPagos(ingresoPorFacturas);

        return ingreso;
    }

    public List<FacturasPorEmpleado> obtenerFacturasporVendedor( Date fechaInicio, Date fechaFin ) {

        List<FacturasPorEmpleado> lstFacturas = new ArrayList<FacturasPorEmpleado>();
        QModificacion mod = QModificacion.modificacion;
        List<Modificacion> lstModificaciones = (List<Modificacion>) modificacionRepository.findAll( mod.fecha.between(fechaInicio, fechaFin),
                mod.idEmpleado.asc(), mod.fecha.asc() );
        for ( Modificacion modificacion : lstModificaciones ) {
            String IdEmpleado = modificacion.getNotaVenta().getIdEmpleado();
            FacturasPorEmpleado factura = FindOrCreate( lstFacturas, IdEmpleado );
            factura.AcumulaCancelaciones( modificacion );
        }
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and(nv.factura.isNotEmpty().and(nv.factura.isNotNull())));
        for ( NotaVenta venta : lstVentas ) {
            String IdEmpleado = venta.getIdEmpleado();
            FacturasPorEmpleado factura = FindOrCreate( lstFacturas, IdEmpleado );
            factura.AcumulaVentas();
        }
        return lstFacturas;
    }

    public FacturasPorEmpleado FindOrCreate( List<FacturasPorEmpleado> lstFacturas, String idEmpleado ) {
        FacturasPorEmpleado found = null;

        for ( FacturasPorEmpleado facturas : lstFacturas ) {
            if ( facturas.getIdEmpleado().equals( idEmpleado ) ) {
                found = facturas;
                break;
            }
        }
        if ( found == null ) {
            found = new FacturasPorEmpleado( idEmpleado );
            Empleado empleado = empleadoRepository.findOne( idEmpleado );
            if ( empleado != null ) {
                found.setNombre( empleado.nombreCompleto() );
            }
            lstFacturas.add( found );
        }
        return found;
    }

    public List<NotaVenta> obtenerVentasLineaporFacturas( Date fechaInicio, Date fechaFin, String articulo, boolean gogle, boolean oftalmico, boolean todo ) {

        QNotaVenta venta = QNotaVenta.notaVenta;
        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !articulo.equals( null ) && !articulo.isEmpty() && articulo.length() > 0 ) {
            builderArt.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() ).and( venta.detalles.any().articulo.articulo.eq( articulo ) );
        } else {
            builderArt.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( venta.detalles.any().articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( venta.detalles.any().articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() );
        } else {
            builder.and( venta.factura.isNotNull() ).and( venta.factura.isNotEmpty() );
        }

        List<NotaVenta> lstArticulos = ( List<NotaVenta> ) notaVentaRepository.findAll( venta.fechaHoraFactura.between( fechaInicio, fechaFin ).
                and( builderArt ).and( builderOft ).and( builderGogle ).and( builder ) );

        return lstArticulos;

    }

    public List<FacturasPorEmpleado> obtenerVentasLineaporArticulos( Date fechaInicio, Date fechaFin, String articulo, boolean gogle, boolean oftalmico, boolean todo ) {

        QDetalleNotaVenta venta = QDetalleNotaVenta.detalleNotaVenta;

        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !articulo.equals( null ) && !articulo.isEmpty() && articulo.length() > 0 ) {
            builderArt.and( venta.articulo.articulo.eq( articulo ) );
        } else {
            builderArt.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( venta.articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( venta.articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( venta.precioUnitFinal.isNotNull() );
        } else {
            builder.and( venta.precioUnitFinal.isNotNull() );
        }

        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = iva.getTasa();
        List<FacturasPorEmpleado> lstArticulos = new ArrayList<FacturasPorEmpleado>();
        List<DetalleNotaVenta> lstArticulo = ( List<DetalleNotaVenta> ) detalleNotaVentaRepository.findAll( venta.notaVenta.fechaHoraFactura.between( fechaInicio, fechaFin ).
                and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ).and(venta.precioUnitFinal.ne(BigDecimal.ZERO)).
                and( venta.notaVenta.factura.isNotEmpty()).and( venta.notaVenta.factura.isNotNull()), venta.articulo.id.asc() );
        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstModificacion = ( List<Modificacion>) modificacionRepository.findAll( modificacion.fecha.between( fechaInicio, fechaFin ) );

        for ( DetalleNotaVenta ventas : lstArticulo ) {
            String art = ventas.getArticulo().getArticulo();
            FacturasPorEmpleado idArt = FindOrCreated( lstArticulos, art );
            idArt.AcumulaArticulos( false, ventas, ivaTasa, "" );
        }

        for( Modificacion mod : lstModificacion ){
            for(DetalleNotaVenta det : mod.getNotaVenta().getDetalles()){
                if( det.getPrecioUnitFinal().compareTo( BigDecimal.ZERO ) > 0 ){
                    Articulo article = articuloRepository.findOne( det.getIdArticulo() );
                    FacturasPorEmpleado art = FindOrCreated( lstArticulos, article.getDescripcion() );
                    art.AcumulaCancelaciones( det, ivaTasa );
                }
            }
        }

        return lstArticulos;
    }

    public FacturasPorEmpleado FindOrCreated( List<FacturasPorEmpleado> lstFacturas, String art ) {
        FacturasPorEmpleado found = null;

        for ( FacturasPorEmpleado articulos : lstFacturas ) {
            if ( art.equals( articulos.getArticulo() ) ) {
                found = articulos;
                break;
            }
        }
        if ( found == null ) {
            found = new FacturasPorEmpleado( art );
            lstFacturas.add( found );
        }
        return found;
    }

    public List<FacturasPorEmpleado> obtenerVentasMarca( Date fechaInicio, Date fechaFin, String marca, boolean noMostrarArticulos, boolean gogle, boolean oftalmico, boolean todo ) {

        QDetalleNotaVenta venta = QDetalleNotaVenta.detalleNotaVenta;
        log.info( "Verifica que se halla seleccionado un articulo especifico" );

        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !marca.equals( null ) && !marca.isEmpty() && marca.length() > 0 ) {
            builderArt.and( venta.articulo.marca.eq( marca ) );
        } else {
            builderArt.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( venta.articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( venta.articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( venta.precioUnitFinal.isNotNull() );
        } else {
            builder.and( venta.precioUnitFinal.isNotNull() );
        }

        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = ( iva.getTasa() ) / 100;
        List<FacturasPorEmpleado> lstArticulos = new ArrayList<FacturasPorEmpleado>();
        List<DetalleNotaVenta> lstArticulo = ( List<DetalleNotaVenta> ) detalleNotaVentaRepository.findAll( venta.notaVenta.fechaHoraFactura.between( fechaInicio, fechaFin ).
                and( venta.notaVenta.factura.isNotNull() ).and( venta.notaVenta.factura.isNotEmpty() ).and(venta.precioUnitLista.ne(BigDecimal.ZERO)).
                and(venta.notaVenta.sFactura.ne(TAG_CANCELADO)).and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ), venta.articulo.marca.asc() );

        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion> ) modificacionRepository.findAll( modificacion.fecha.between(fechaInicio,fechaFin).
                and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)));

        for ( DetalleNotaVenta ventas : lstArticulo ) {
            String art = ventas.getArticulo().getMarca();
            String idArticulo = String.format( "[%s] %s", ventas.getIdArticulo(), ventas.getArticulo().getArticulo() );
            if( BigDecimal.ZERO.compareTo(ventas.getPrecioUnitLista()) < 0 ){
                FacturasPorEmpleado idArt = FindorCreated( lstArticulos, art );
                idArt.AcumulaArticulos( noMostrarArticulos, ventas, ivaTasa, idArticulo );
            }
        }

        for( Modificacion mod : lstCancelaciones ){
            if( StringUtils.trimToEmpty(marca).length() > 0 ){
                List<DetalleNotaVenta> lstDetalles = new ArrayList<DetalleNotaVenta>(mod.getNotaVenta().getDetalles());
                Collections.sort(lstDetalles, new Comparator<DetalleNotaVenta>() {
                    @Override
                    public int compare( DetalleNotaVenta o1, DetalleNotaVenta o2 ) {
                        return o1.getArticulo().getIdGenTipo().compareTo(o2.getArticulo().getIdGenTipo());
                    }
                });
                for( DetalleNotaVenta ventas : lstDetalles ){
                    if( marca.equalsIgnoreCase(ventas.getArticulo().getMarca()) ){
                        String art = ventas.getArticulo().getMarca();
                        String idArticulo = String.format( "[%s] %s", ventas.getIdArticulo(), ventas.getArticulo().getArticulo() );
                        if( BigDecimal.ZERO.compareTo(ventas.getPrecioUnitLista()) < 0 ){
                            FacturasPorEmpleado idArt = FindorCreated( lstArticulos, art );
                            idArt.AcumulaArticulosCancelados( noMostrarArticulos, ventas, ivaTasa, idArticulo );
                        }
                    }
                }
            } else {
                List<DetalleNotaVenta> lstDetalles = new ArrayList<DetalleNotaVenta>(mod.getNotaVenta().getDetalles());
                Collections.sort(lstDetalles, new Comparator<DetalleNotaVenta>() {
                    @Override
                    public int compare( DetalleNotaVenta o1, DetalleNotaVenta o2 ) {
                        return o1.getArticulo().getIdGenTipo().compareTo(o2.getArticulo().getIdGenTipo());
                    }
                });
                for( DetalleNotaVenta ventas : lstDetalles ){
                    String art = ventas.getArticulo().getMarca();
                    String idArticulo = String.format( "[%s] %s", ventas.getIdArticulo(), ventas.getArticulo().getArticulo() );
                    if( BigDecimal.ZERO.compareTo(ventas.getPrecioUnitLista()) < 0 ){
                        FacturasPorEmpleado idArt = FindorCreated( lstArticulos, art );
                        idArt.AcumulaArticulosCancelados( noMostrarArticulos, ventas, ivaTasa, idArticulo );
                    }
                }
            }
        }

        Collections.sort( lstArticulo, new Comparator<DetalleNotaVenta>() {
            @Override
            public int compare(DetalleNotaVenta o1, DetalleNotaVenta o2) {
                return o1.getIdFactura().compareTo(o2.getIdFactura());
            }
        });
        String idFactura = " ";
        Boolean isNotaCredito = false;
        for( DetalleNotaVenta notaCredito : lstArticulo ) {
            if( (!idFactura.equalsIgnoreCase(notaCredito.getIdFactura())) || (idFactura.equalsIgnoreCase(notaCredito.getIdFactura()) && isNotaCredito) ){
                    for( Pago pago : notaCredito.getNotaVenta().getPagos() ){
                        if( pago.getIdFormaPago().equalsIgnoreCase("NOT") ){
                            String art = notaCredito.getArticulo().getMarca();
                            if( BigDecimal.ZERO.compareTo(notaCredito.getPrecioUnitLista()) < 0 ){
                                FacturasPorEmpleado idArt = FindorCreated( lstArticulos, art );
                                idArt.AcumulaMontoNotasCredito( notaCredito, pago.getMonto(), ivaTasa );
                            }
                            isNotaCredito = true;
                        }
                    }
                }
            for( Pago pago : notaCredito.getNotaVenta().getPagos() ){
                if( pago.getIdFormaPago().equalsIgnoreCase("NOT") ){
                    String art = notaCredito.getArticulo().getMarca();
                    if( BigDecimal.ZERO.compareTo(notaCredito.getPrecioUnitLista()) < 0 ){
                        FacturasPorEmpleado idArt = FindorCreated( lstArticulos, art );
                        idArt.AcumulaArticulosNotasCredito( notaCredito, pago.getMonto(), ivaTasa );
                    }
                }
            }
            idFactura = notaCredito.getIdFactura();
        }

        return lstArticulos;
    }

    public FacturasPorEmpleado FindorCreated( List<FacturasPorEmpleado> lstFacturas, String art ) {
        FacturasPorEmpleado found = null;

        for ( FacturasPorEmpleado articulos : lstFacturas ) {
            if ( articulos.getMarca().equals( art ) ) {
                found = articulos;
                break;
            }
        }
        if ( found == null ) {
            found = new FacturasPorEmpleado( art );
            lstFacturas.add( found );
        }
        return found;
    }

    public List<IngresoPorVendedor> obtenerVentasporVendedorporMarca( Date fechaInicio, Date fechaFin, String marca, boolean mostrarArticulos, boolean gogle, boolean oftalmico, boolean todo ) {
        log.info( "obtenerVentasporVendedor()" );

        log.info( "Se obtiene elvalor del IVA" );
        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = ( iva.getTasa() ) / 100;

        QDetalleNotaVenta venta = QDetalleNotaVenta.detalleNotaVenta;

        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !marca.equals( null ) && !marca.isEmpty() && marca.length() > 0 ) {
            builderArt.and( venta.articulo.marca.eq( marca ) );
        } else {
            builderArt.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( venta.articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( venta.articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( venta.precioUnitFinal.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( venta.precioUnitFinal.isNotNull() );
        } else {
            builder.and( venta.precioUnitFinal.isNotNull() );
        }

        List<IngresoPorVendedor> lstIngresos = new ArrayList<IngresoPorVendedor>();
        List<DetalleNotaVenta> lstVentas = ( List<DetalleNotaVenta> ) detalleNotaVentaRepository.findAll( venta.notaVenta.fechaHoraFactura.between( fechaInicio, fechaFin ).
                and( venta.notaVenta.factura.isNotNull() ).and( venta.notaVenta.factura.isNotEmpty() ).
                and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ).and(venta.precioUnitLista.ne(BigDecimal.ZERO)).
                and(venta.notaVenta.sFactura.ne(TAG_CANCELADO)).and(venta.precioUnitLista.ne(BigDecimal.ZERO)),
                venta.notaVenta.idEmpleado.asc(), venta.articulo.marca.asc() );

        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll(nv.fechaHoraFactura.between(fechaInicio,fechaFin).
                and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull()).and(nv.sFactura.ne(TAG_CANCELADO)));

        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion> ) modificacionRepository.findAll(modificacion.fecha.between(fechaInicio,fechaFin).
                and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)));

        for ( DetalleNotaVenta ventas : lstVentas ) {
            if ( ventas.getNotaVenta().getFactura() != null && !ventas.getNotaVenta().getsFactura().equals( "T" ) ) {
                String articulo = String.format( "[%s] %s", ventas.getIdArticulo().toString(), ventas.getArticulo().getArticulo() );
                /*if( articulo.length() > 20 ){
                    articulo = articulo.substring(0, 20);
                }*/
                String idEmpleado = ventas.getNotaVenta().getIdEmpleado();
                IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                ingreso.AcumulaPagos( mostrarArticulos, articulo, ventas, ventas.getNotaVenta().getFechaHoraFactura(), ventas.getArticulo().getMarca(), ventas.getPrecioUnitFinal(), ivaTasa, ventas.getArticulo().getIdGenTipo() );
            }
        }

        for( Modificacion mod : lstCancelaciones){
            if( StringUtils.trimToEmpty(marca).length() > 0 ){
                    String idEmpleado = mod.getNotaVenta().getIdEmpleado();
                    IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                    for(DetalleNotaVenta ventas : mod.getNotaVenta().getDetalles()){
                        if( marca.equalsIgnoreCase(ventas.getArticulo().getMarca()) && ventas.getPrecioUnitLista().compareTo(BigDecimal.ZERO) > 0 ){
                        String articulo = String.format( "[%s] %s", ventas.getIdArticulo().toString(), ventas.getArticulo().getArticulo() );
                        ingreso.AcumulaPagosCan( ventas, articulo, ventas.getNotaVenta().getFechaHoraFactura(), ventas.getArticulo().getMarca(), ventas.getPrecioUnitFinal(), ivaTasa, ventas.getArticulo().getIdGenTipo() );
                        }
                }
            } else {
                String idEmpleado = mod.getNotaVenta().getIdEmpleado();
                IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                for(DetalleNotaVenta ventas : mod.getNotaVenta().getDetalles()){
                    if( ventas.getPrecioUnitLista().compareTo(BigDecimal.ZERO) > 0 ){
                        String articulo = String.format( "[%s] %s", ventas.getIdArticulo().toString(), ventas.getArticulo().getArticulo() );
                        ingreso.AcumulaPagosCan( ventas, articulo, ventas.getNotaVenta().getFechaHoraFactura(), ventas.getArticulo().getMarca(), ventas.getPrecioUnitFinal(), ivaTasa, ventas.getArticulo().getIdGenTipo() );
                    }
                }
            }
        }

        Boolean isNotaCredito = false;
        String idFactura = " ";
        for( DetalleNotaVenta notaVenta : lstVentas ){
            if( StringUtils.trimToEmpty(marca).length() > 0 ){
                for( Pago pago : notaVenta.getNotaVenta().getPagos() ){
                    if( pago.getIdFPago().equalsIgnoreCase("NOT") ){
                            if( (!idFactura.equalsIgnoreCase(notaVenta.getIdFactura())) || (idFactura.equalsIgnoreCase(notaVenta.getIdFactura()) && isNotaCredito ) ){
                                if( marca.equalsIgnoreCase(notaVenta.getArticulo().getMarca()) ){
                                    String idEmpleado = pago.getNotaVenta().getIdEmpleado();
                                    IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                                    Integer cantArticulos = notaVenta.getCantidadFac().intValue();
                                    ingreso.AcumulaPagosNotaCredito( cantArticulos, notaVenta, notaVenta.getNotaVenta().getFechaHoraFactura(), notaVenta.getArticulo().getMarca(), pago.getMonto(), ivaTasa, notaVenta.getArticulo().getIdGenTipo() );
                                }
                            }
                            if( marca.equalsIgnoreCase(notaVenta.getArticulo().getMarca()) ){
                                String idEmpleado = pago.getNotaVenta().getIdEmpleado();
                                IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                                Integer cantArticulos = notaVenta.getCantidadFac().intValue();
                                ingreso.AcumulaArticulosNotaCredito( cantArticulos, notaVenta, notaVenta.getNotaVenta().getFechaHoraFactura(), notaVenta.getArticulo().getMarca(), pago.getMonto(), ivaTasa, notaVenta.getArticulo().getIdGenTipo() );
                            }
                            idFactura = pago.getIdFactura();
                            isNotaCredito = true;
                    }
                }
            } else {
                for( Pago pago : notaVenta.getNotaVenta().getPagos() ){
                    if( pago.getIdFPago().equalsIgnoreCase("NOT") ){
                            if( !idFactura.equalsIgnoreCase(notaVenta.getIdFactura()) || (idFactura.equalsIgnoreCase(notaVenta.getIdFactura()) && isNotaCredito) ){
                                String idEmpleado = pago.getNotaVenta().getIdEmpleado();
                                IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                                Integer cantArticulos = notaVenta.getCantidadFac().intValue();
                                ingreso.AcumulaPagosNotaCredito( cantArticulos, notaVenta, notaVenta.getNotaVenta().getFechaHoraFactura(), notaVenta.getArticulo().getMarca(), pago.getMonto(), ivaTasa, notaVenta.getArticulo().getIdGenTipo() );
                            }
                            String idEmpleado = pago.getNotaVenta().getIdEmpleado();
                            IngresoPorVendedor ingreso = FindorCreate( lstIngresos, idEmpleado );
                            Integer cantArticulos = notaVenta.getCantidadFac().intValue();
                            ingreso.AcumulaArticulosNotaCredito( cantArticulos, notaVenta, notaVenta.getNotaVenta().getFechaHoraFactura(), notaVenta.getArticulo().getMarca(), pago.getMonto(), ivaTasa, notaVenta.getArticulo().getIdGenTipo() );
                            idFactura = pago.getIdFactura();
                            isNotaCredito = true;
                    }
                }
            }

        }

        for(IngresoPorVendedor ingreso : lstIngresos){
            Collections.sort( ingreso.getPagos(), new Comparator<IngresoPorFactura>() {
                @Override
                public int compare(IngresoPorFactura o1, IngresoPorFactura o2) {
                    return o1.getFechaPago().compareTo(o2.getFechaPago());
                }
            });
        }
        return lstIngresos;
    }

    public List<Articulo> obtenerExistenciasporMarcaDetallado( String marca, boolean gogle, boolean oftalmico, boolean todo ) {
        log.info( "obtenerVentasporVendedor()" );


        QArticulo articulo = QArticulo.articulo1;

        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !marca.equals( null ) && !marca.isEmpty() && marca.length() > 0 ) {
            builderArt.and( articulo.idGenSubtipo.equalsIgnoreCase( marca ).or( articulo.articulo.equalsIgnoreCase( marca ) ) );
        } else {
            builderArt.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( articulo.id.isNotNull() );
        } else {
            builder.and( articulo.id.isNotNull() );
        }

        List<Articulo> lstArticulos = new ArrayList<Articulo>();
        List<Articulo> lstArticulo = ( List<Articulo> ) articuloRepository.findAll( articulo.cantExistencia.isNotNull().and( articulo.cantExistencia.ne( 0 ) ).
                and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ).and( articulo.idGenerico.equalsIgnoreCase( "A" ) ),
                articulo.idGenTipo.asc() );
        log.info( "tamañoLista:{}", lstArticulos.size() );

        for ( Articulo articulos : lstArticulo ) {
            if ( articulos.getCantExistencia() > 0 || articulos.getCantExistencia() < 0 ) {
                lstArticulos.add( articulos );
            }
        }

        return lstArticulos;
    }

    public List<FacturasPorEmpleado> obtenerExistenciasporMarcaCompleto( String marca, boolean gogle, boolean oftalmico, boolean todo ) {
        log.info( "obtenerVentasporVendedor()" );

        QArticulo articulo = QArticulo.articulo1;
        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !StringUtils.trimToEmpty( marca ).isEmpty() ) {
            builderArt.and( articulo.marca.like( marca + "%" ) );
        } else {
            builderArt.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( articulo.id.isNotNull() );
        } else {
            builder.and( articulo.id.isNotNull() );
        }

        List<FacturasPorEmpleado> lstArticulos = new ArrayList<FacturasPorEmpleado>();
        List<Articulo> lstArticulo = new ArrayList<Articulo>();
        List<Articulo> lstArt = ( List<Articulo> ) articuloRepository.findAll( articulo.cantExistencia.isNotNull().
                and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ), articulo.marca.asc() );

        for ( Articulo artic : lstArt ) {
            if ( artic.getCantExistencia() > 0 ) {
                if( artic.getDescripcion().length() >= 60 ){
                    artic.setDescripcion( artic.getDescripcion().substring(0,60));
                }
                lstArticulo.add( artic );
            }
        }

        for ( Articulo articulos : lstArticulo ) {
            String linea = articulos.getMarca();
            Precio precio = new Precio();
            precio.setPrecio( BigDecimal.ZERO );
            QPrecio price = QPrecio.precio1;
            List<Precio> precios = (List<Precio>) precioRepository.findAll(price.articulo.trim().eq(articulos.getArticulo()));
            if(precios.size() == 1){
              precio = precios.get(0);
            }
            FacturasPorEmpleado facturas = FindOorCreate( lstArticulos, linea );
            facturas.AcumulaMarcas( articulos.getMarca(), articulos, precio );
            Collections.sort( facturas.getFacturasVendedor(), new Comparator<IngresoPorFactura>() {
                @Override
                public int compare(IngresoPorFactura o1, IngresoPorFactura o2) {
                    return o1.getMarca().compareTo(o2.getMarca());
                }
            } );
        }

        return lstArticulos;
    }


    public List<FacturasPorEmpleado> obtenerExistenciasporMarcaResumido( String marca, boolean gogle, boolean oftalmico, boolean todo ) {
        log.info( "obtenerVentasporVendedor()" );

        QArticulo articulo = QArticulo.articulo1;
        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !StringUtils.trimToEmpty( marca ).isEmpty() ) {
            builderArt.and( articulo.marca.like( marca + "%" ) );
        } else {
            builderArt.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderGogle = new BooleanBuilder();
        if ( gogle ) {
            builderGogle.and( articulo.idGenTipo.eq( "G" ) );
        } else {
            builderGogle.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderOft = new BooleanBuilder();
        if ( oftalmico ) {
            builderOft.and( articulo.idGenTipo.eq( "O" ) );
        } else {
            builderOft.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builder = new BooleanBuilder();
        if ( todo ) {
            builder.and( articulo.id.isNotNull() );
        } else {
            builder.and( articulo.id.isNotNull() );
        }

        List<FacturasPorEmpleado> lstArticulos = new ArrayList<FacturasPorEmpleado>();
        List<Articulo> lstArticulo = new ArrayList<Articulo>();
        List<Articulo> lstArt = ( List<Articulo> ) articuloRepository.findAll( articulo.cantExistencia.isNotNull().
                and( builder ).and( builderOft ).and( builderGogle ).and( builderArt ), articulo.idGenerico.asc() );

        for ( Articulo artic : lstArt ) {
            if ( artic.getCantExistencia() > 0 || artic.getCantExistencia() < 0 ) {
                if( artic.getDescripcion().length() >= 60 ){
                    artic.setDescripcion( artic.getDescripcion().substring(0,60));
                }
                lstArticulo.add( artic );
            }
        }

        for ( Articulo articulos : lstArticulo ) {
            String linea = articulos.getIdGenerico();
            FacturasPorEmpleado facturas = FindOorCreate( lstArticulos, linea );
            facturas.AcumulaMarcasResumido( articulos.getMarca(), articulos );
            Collections.sort( facturas.getFacturasVendedor(), new Comparator<IngresoPorFactura>() {
                @Override
                public int compare(IngresoPorFactura o1, IngresoPorFactura o2) {
                    return o1.getMarca().compareTo(o2.getMarca());
                }
            } );
        }

        return lstArticulos;
    }

    public FacturasPorEmpleado FindOorCreate( List<FacturasPorEmpleado> lstFacturas, String idEmpleado ) {
        FacturasPorEmpleado found = null;
        for ( FacturasPorEmpleado facturas : lstFacturas ) {
            if ( facturas.getIdEmpleado().equals( idEmpleado ) ) {
                found = facturas;
                break;
            }
        }
        if ( found == null ) {
            found = new FacturasPorEmpleado( idEmpleado );
            lstFacturas.add( found );
        }
        return found;
    }

    public List<Articulo> obtenerExistenciasporArticulo( String marca, String descripcion, String color ) {
        log.info( "obtenerVentasporVendedor()" );

        QArticulo articulo = QArticulo.articulo1;
        log.info( "Verifica que se halla seleccionado un articulo especifico" );
        BooleanBuilder builderArt = new BooleanBuilder();
        if ( !StringUtils.trimToEmpty( marca ).isEmpty() ) {
            builderArt.and( articulo.articulo.like( marca + "%" ) );
        } else {
            builderArt.and( articulo.id.isNotNull() );
        }

        BooleanBuilder builderCol = new BooleanBuilder();
        if ( !StringUtils.trimToEmpty( color ).isEmpty() ) {
            builderCol.and( articulo.codigoColor.like( color + "%" ) );
        } else {
            builderCol.and( articulo.id.isNotNull() );
        }

        List<Articulo> lstArticulos = new ArrayList<Articulo>();
        List<Articulo> lstArticulo = ( List<Articulo> ) articuloRepository.findAll( articulo.cantExistencia.isNotNull().
                and( builderArt ).and( builderCol ), articulo.id.asc() );
        log.info( "tamañoLista:{}", lstArticulos.size() );

        for ( Articulo articulos : lstArticulo ) {
            if ( articulos.getCantExistencia() > 0 || articulos.getCantExistencia() < 0 ) {
                QPrecio price = QPrecio.precio1;
                List <Precio> precio = (List<Precio>) precioRepository.findAll(price.articulo.trim().eq(articulos.getArticulo()));
                if(precio.size() == 1){
                  articulos.setPrecio( precio.get(0).getPrecio() );
                }
                if( articulos.getDescripcion().length() >= 55 ){
                    articulos.setDescripcion( articulos.getDescripcion().substring(0,50) );
                }
                lstArticulos.add( articulos );
            }
        }

        String desc = StringUtils.trimToEmpty( descripcion ).toUpperCase();
        if( desc.length() > 0 ){
            List<Articulo> articulos = new ArrayList<Articulo>();
            articulos.addAll( lstArticulos );
            lstArticulos.clear();
            for( Articulo art : articulos ){
                if( art.getDescripcion().toUpperCase().contains( desc ) ) {
                    if( art.getDescripcion().length() >= 55 ){
                        art.setDescripcion( art.getDescripcion().substring(0,50));
                    }
                    lstArticulos.add( art );
                }
            }
        }

        Collections.sort( lstArticulos, new Comparator<Articulo>() {
            @Override
            public int compare(Articulo o1, Articulo o2) {
                return o1.getArticulo().compareTo(o2.getArticulo());
            }
        });

        return lstArticulos;
    }

    public List<SaldoPorEstado> obtenerTrabajos( boolean retenidos, boolean porEnviar, boolean pino, boolean sucursal, boolean todos, boolean factura, boolean fechaPromesa ) {

        QTrabajo trabajo = QTrabajo.trabajo;
        BooleanBuilder builderRet = new BooleanBuilder();
        if ( retenidos ) {
            builderRet.and( trabajo.estado.equalsIgnoreCase( "RTN" ) );
        } else {
            builderRet.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty() );
        }

        BooleanBuilder builderPorEnv = new BooleanBuilder();
        if ( porEnviar ) {
            builderPorEnv.and( trabajo.estado.equalsIgnoreCase( "PE" ).or( trabajo.estado.equalsIgnoreCase( "RPE" ) ).
                    or( trabajo.estado.equalsIgnoreCase( "X1" ) ) );
        } else {
            builderPorEnv.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty() );
        }

        BooleanBuilder builderPino = new BooleanBuilder();
        if ( pino ) {
            builderPino.and( trabajo.estado.equalsIgnoreCase( "EP" ).or( trabajo.estado.equalsIgnoreCase( "REP" ) ) );
        } else {
            builderPino.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty() );
        }

        BooleanBuilder builderSuc = new BooleanBuilder();
        if ( sucursal ) {
            builderSuc.and( trabajo.estado.equalsIgnoreCase( "RS" ).or( trabajo.estado.equalsIgnoreCase( "X3" ) ) );
        } else {
            builderSuc.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty() );
        }

        BooleanBuilder builderTodo = new BooleanBuilder();
        if ( todos ) {
            builderTodo.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty().and( trabajo.estado.ne( "TE" ).and( trabajo.estado.ne( "CN" ) ) ) );
        } else {
            builderTodo.and( trabajo.estado.isNotNull() ).and( trabajo.estado.isNotEmpty().and( trabajo.estado.ne( "TE" ).and( trabajo.estado.ne( "CN" ) ) ) );
        }

        OrderSpecifier<String> fact = null;
        if ( factura ) {
            fact = trabajo.id.asc();
        } else {
            fact = trabajo.id.desc();
        }

        OrderSpecifier<Date> fechProm = null;
        if ( fechaPromesa ) {
            fechProm = trabajo.fechaPromesa.asc();
        } else {
            fechProm = trabajo.fechaPromesa.desc();
        }

        List<SaldoPorEstado> lstTrabajos = new ArrayList<SaldoPorEstado>();
        List<Trabajo> lstTrabajo = ( List<Trabajo> ) trabajoRepository.findAll( trabajo.jbTipo.ne( "GRUPO" ).and( builderTodo ).and( builderSuc ).and( builderPino ).
                and( builderRet ).and( builderPorEnv ).and( trabajo.estado.ne( "BD" ) ), fact, fechProm, trabajo.jbTipo.desc() );
        log.info( "tamañoLista:{}", lstTrabajo.size() );
        for ( Trabajo trabajos : lstTrabajo ) {
            String estado = trabajos.getTrabajoEstado().getDescr();
            SaldoPorEstado saldo = FindoorCreate( lstTrabajos, estado );
            saldo.AcumulaSaldos( trabajos );
        }

        return lstTrabajos;
    }

    public SaldoPorEstado FindoorCreate( List<SaldoPorEstado> lstSaldos, String estado ) {

        SaldoPorEstado found = null;

        for ( SaldoPorEstado saldos : lstSaldos ) {

            if ( saldos.getEstado().equals( estado ) ) {

                found = saldos;
                break;
            }
        }
        if ( found == null ) {
            found = new SaldoPorEstado( estado );
            lstSaldos.add( found );
        }
        return found;
    }

    public List<TrabajoTrack> obtenerTrabajosporEntregar( Date fechaInicio, Date fechaFin ) {

        QTrabajoTrack trabajo = QTrabajoTrack.trabajoTrack;
        List<TrabajoTrack> lstTrabajos = ( List<TrabajoTrack> ) trabajoTrackRepository.findAll( trabajo.fecha.between( fechaInicio, fechaFin ).
                and( trabajo.estado.equalsIgnoreCase( "TE" ) ).and( trabajo.trabajo.estado.equalsIgnoreCase( "TE" ) ) );
        log.info( "tamañoListas:{}", lstTrabajos.size() );

        return lstTrabajos;
    }

    public List<SaldoPorEstado> obtenerTrabajosporEntregarporEmpleado( Date fechaInicio, Date fechaFin ) {

        QTrabajoTrack trabajo = QTrabajoTrack.trabajoTrack;
        List<SaldoPorEstado> lstTrabajos = new ArrayList<SaldoPorEstado>();
        List<TrabajoTrack> lstTrabajo = ( List<TrabajoTrack> ) trabajoTrackRepository.findAll( trabajo.fecha.between( fechaInicio, fechaFin ).
                and( trabajo.estado.equalsIgnoreCase( "TE" ) ).and( trabajo.trabajo.estado.equalsIgnoreCase( "TE" ) ), trabajo.fecha.asc() );
        log.info( "tamañoListas:{}", lstTrabajo.size() );

        for ( TrabajoTrack trabajos : lstTrabajo ) {
            String empleado = trabajos.getTrabajo().getEmpAtendio();
            SaldoPorEstado saldo = FindoOrCreate( lstTrabajos, empleado );
            saldo.AcumulaTrabajos( trabajos, lstTrabajo.size() );
        }

        return lstTrabajos;
    }

    public SaldoPorEstado FindoOrCreate( List<SaldoPorEstado> lstSaldos, String empleado ) {

        SaldoPorEstado found = null;

        for ( SaldoPorEstado saldos : lstSaldos ) {

            if ( saldos.getIdEmpleado().equals( empleado ) ) {
                found = saldos;
                break;
            }
        }
        if ( found == null ) {
            found = new SaldoPorEstado( empleado );
            Empleado empleados = empleadoRepository.findOne( empleado );
            if ( empleado != null ) {
                found.setNomEmpleado( empleados.nombreCompleto() );
            }
            lstSaldos.add( found );
        }
        return found;
    }

    public List<IngresoPorDia> obtenerVentasporDia( Date fechaInicio, Date fechaFin ) {

        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = iva.getTasa();

        List<IngresoPorDia> lstVentas = new ArrayList<IngresoPorDia>();
        QNotaVenta ventas = QNotaVenta.notaVenta;
        List<NotaVenta> lstVenta = ( List<NotaVenta> ) notaVentaRepository.findAll( ventas.fechaHoraFactura.between( fechaInicio, fechaFin ).and( ventas.factura.isNotNull() ),
                ventas.fechaHoraFactura.asc() );
        QModificacion mod = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion>) modificacionRepository.findAll( mod.fecha.between(fechaInicio,fechaFin).
                and(mod.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)));

        for ( NotaVenta venta : lstVenta ) {
            IngresoPorDia ingreso = FindOrCreate( lstVentas, venta.getFechaHoraFactura() );
            ingreso.AcumulaMonto( venta.getVentaTotal(), ivaTasa );
        }

        return lstVentas;
    }

    public List<DescuentosPorTipo> obtenerDescuentosporTipo( Date fechaInicio, Date fechaFin ) {

        List<DescuentosPorTipo> lstDescuentos = new ArrayList<DescuentosPorTipo>();
        QDescuento descuento = QDescuento.descuento;
        List<Descuento> lstDescuento = ( List<Descuento> ) descuentoRepository.findAll( descuento.fecha.between( fechaInicio, fechaFin ) );
        Integer noDesc = lstDescuento.size();
        for ( Descuento descuentos : lstDescuento ) {
            DescuentosPorTipo desc = FindOoorCreate( lstDescuentos, descuentos.getTipoClave() );
            desc.AcumulaDescuentos( descuentos, noDesc );
        }

        return lstDescuentos;
    }

    public DescuentosPorTipo FindOoorCreate( List<DescuentosPorTipo> lstDescuentos, String tipo ) {
        DescuentosPorTipo found = null;
        for ( DescuentosPorTipo desc : lstDescuentos ) {
            if ( desc.getTipo().equalsIgnoreCase( tipo ) ) {
                found = desc;
                break;
            }
        }
        if ( found == null ) {
            found = new DescuentosPorTipo( tipo );
            lstDescuentos.add( found );
        }
        return found;
    }

    public List<DescuentosPorTipo> obtenerPagosporTipo( Date fechaInicio, Date fechaFin, String formaPago, String factura ) {

        List<DescuentosPorTipo> lstPagos = new ArrayList<DescuentosPorTipo>();
        QPago pago = QPago.pago;

        BooleanBuilder builderformaPago = new BooleanBuilder();
        if ( !formaPago.equals( null ) && !formaPago.isEmpty() && formaPago.length() > 0 ) {
            builderformaPago.and( pago.idFormaPago.equalsIgnoreCase( formaPago ) );
        } else {
            builderformaPago.and( pago.idFactura.isNotNull() );
        }

        BooleanBuilder builderFactura = new BooleanBuilder();
        if ( !factura.equals( null ) && !factura.isEmpty() && factura.length() > 0 ) {
            builderFactura.and( pago.notaVenta.factura.equalsIgnoreCase( factura ) );
        } else {
            builderFactura.and( pago.notaVenta.factura.isNotNull().and( pago.notaVenta.factura.isNotEmpty() ) );
        }

        List<Pago> lstPago = ( List<Pago> ) pagoRepository.findAll( pago.fecha.between( fechaInicio, fechaFin ).
                and( builderformaPago ).and( builderFactura ) );
        QBancoEmisor banco = QBancoEmisor.bancoEmisor;
        for ( Pago pagos : lstPago ) {
            String descPago = tipoPagoRepository.findOne( pagos.getIdFPago() ).getDescripcion();
            BancoEmisor bancos = new BancoEmisor();
            Boolean esPagoDolares = Registry.isCardPaymentInDollars(pagos.geteTipoPago().getId());
            if ( !StringUtils.trimToEmpty(pagos.getIdBancoEmisor()).equalsIgnoreCase("") ) {
                Boolean idBancNum = false;
                Integer idBanco = 0;
                try{
                 idBanco = Integer.parseInt( pagos.getIdBancoEmisor() );
                } catch ( Exception e ) {
                    idBancNum = false;
                }
                if( idBancNum ){
                bancos = ( BancoEmisor ) bancoEmisorRepository.findOne( idBanco );
                }
                DescuentosPorTipo desc = FindeOrCreate( lstPagos, pagos.getIdFPago() );
                desc.AcumulaTipoPagos( pagos, bancos, descPago, esPagoDolares );
                Collections.sort( desc.getDescuentos(), new Comparator<TipoDescuento>() {
                    @Override
                    public int compare( TipoDescuento o1, TipoDescuento o2 ) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                });
            } else {
                DescuentosPorTipo desc = FindeOrCreate( lstPagos, pagos.getIdFPago() );
                desc.AcumulaTipoPagos( pagos, bancos, descPago, esPagoDolares );
                Collections.sort( desc.getDescuentos(), new Comparator<TipoDescuento>() {
                    @Override
                    public int compare( TipoDescuento o1, TipoDescuento o2 ) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                });
            }
        }

        return lstPagos;
    }

    public DescuentosPorTipo FindeOrCreate( List<DescuentosPorTipo> lstPagos, String tipoPago ) {
        DescuentosPorTipo found = null;

        for ( DescuentosPorTipo tipo : lstPagos ) {
            if ( tipo.getTipo().equalsIgnoreCase( tipoPago ) ) {
                found = tipo;
                break;
            }
        }
        if ( found == null ) {
            found = new DescuentosPorTipo( tipoPago );
            lstPagos.add( found );
        }
        return found;
    }

    public List<DescuentosPorTipo> obtenerExamenesporEmpleado( Date fechaInicio, Date fechaFin ) {

        List<DescuentosPorTipo> lstExamenes = new ArrayList<DescuentosPorTipo>();
        QReceta rx = QReceta.receta;
        List<Receta> lstRecetas = (List<Receta>)recetaRepository.findAll( rx.fechaReceta.between(fechaInicio,fechaFin),
                rx.idCliente.asc(), rx.fechaReceta.asc() );
        Integer total = lstRecetas.size();
        for ( Receta receta : lstRecetas ) {
            Examen examen = examenRepository.findOne( receta.getExamen() );
            if( examen != null ){
              String idEmpleado = receta.getIdOptometrista();
              DescuentosPorTipo desc = EncontraroCrear( lstExamenes, idEmpleado );
              desc.AcumulaEmpleados( examen, total, receta );
            }
        }

        return lstExamenes;
    }

    public DescuentosPorTipo EncontraroCrear( List<DescuentosPorTipo> lstExamenes, String idEmpleado ) {
        DescuentosPorTipo found = null;

        for ( DescuentosPorTipo tipo : lstExamenes ) {
            if ( tipo.getIdEmpleado().equals( idEmpleado ) ) {
                found = tipo;
                break;
            }
        }
        if ( found == null ) {
            found = new DescuentosPorTipo( idEmpleado );
            Empleado empleado = empleadoRepository.findOne( idEmpleado );
            if ( empleado != null ) {
                found.setNombreEmpleado( empleado.nombreCompleto() );
            }
            lstExamenes.add( found );
        }
        return found;
    }


    public List<IngresoPorVendedor> obtenerVentasporOptometristaCompleto( Date fechaInicio, Date fechaFin ) {

        Parametro ivaVigenteParam = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        Impuesto iva = impuestoRepository.findOne( ivaVigenteParam.getValor() );
        Double ivaTasa = iva.getTasa() / 100;
        List<IngresoPorVendedor> lstVentas = new ArrayList<IngresoPorVendedor>();

        QNotaVenta venta = QNotaVenta.notaVenta;
        List<NotaVenta> lstVenta = ( List<NotaVenta> ) notaVentaRepository.findAll( venta.fechaHoraFactura.between( fechaInicio, fechaFin ).
                and( venta.sFactura.ne(TAG_CANCELADO) ).and( venta.receta.isNotNull() ).and( venta.factura.isNotEmpty() ).
                and( venta.factura.isNotNull() ), venta.idEmpleado.asc() );
        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = (List<Modificacion>) modificacionRepository.findAll(modificacion.tipo.eq("can").
                and(modificacion.fecha.between(fechaInicio, fechaFin)).and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio, fechaFin)).
                and(modificacion.notaVenta.receta.isNotNull()).
                and(modificacion.notaVenta.factura.isNotNull()).and(modificacion.notaVenta.factura.isNotEmpty()));


        BigDecimal montoTotal = BigDecimal.ZERO;
        Integer totalFacturas = lstVenta.size();
        for ( NotaVenta ventas : lstVenta ) {
            montoTotal = montoTotal.add( ventas.getVentaNeta() );
            String idEmpleado = ventas.getIdEmpleado();
            Receta receta = recetaRepository.findOne( ventas.getReceta() );
            if(receta.getIdOptometrista().toString().trim().equalsIgnoreCase(ventas.getIdEmpleado().trim())){
              IngresoPorVendedor ingresos = FindorCreate( lstVentas, idEmpleado );
              ingresos.AcumulaOptometrista( ventas, montoTotal, totalFacturas, ivaTasa );
            }
        }

        for ( Modificacion mod : lstCancelaciones ) {
            String idEmpleado = mod.getNotaVenta().getIdEmpleado();
            Receta receta = recetaRepository.findOne( mod.getNotaVenta().getReceta() );
            if(receta.getIdOptometrista().toString().trim().equalsIgnoreCase(mod.getNotaVenta().getIdEmpleado().trim())){
                IngresoPorVendedor ingresos = FindorCreate( lstVentas, idEmpleado );
                ingresos.AcumulaCanOptometrista( mod.getNotaVenta(), totalFacturas, ivaTasa );
            }
        }

        return lstVentas;
    }

    public IngresoPorVendedor FindorCreatePrimera( List<IngresoPorVendedor> lstIngresos, String idEmpleado ) {
        IngresoPorVendedor found = null;
        for ( IngresoPorVendedor ingresos : lstIngresos ) {
            if ( ingresos.getIdEmpleado().equals( idEmpleado ) ) {
                found = new IngresoPorVendedor( idEmpleado );
                break;
            }
        }
        if ( found == null ) {
            found = new IngresoPorVendedor( idEmpleado );
            Empleado empleado = empleadoRepository.findOne( idEmpleado );
            if ( empleado != null ) {
                found.setNombre( empleado.nombreCompleto() );
            }
            lstIngresos.add( found );
        }
        return found;
    }


    public IngresoPorVendedor FindorCreateMayor( List<IngresoPorVendedor> lstIngresos, String idEmpleado ) {
        IngresoPorVendedor found = null;
        for ( IngresoPorVendedor ingresos : lstIngresos ) {
            if ( ingresos.getIdEmpleado().equals( idEmpleado ) ) {
                found = ingresos;
                break;
            }
        }
        if ( found == null ) {
            found = new IngresoPorVendedor( idEmpleado );
            Empleado empleado = empleadoRepository.findOne( idEmpleado );
            if ( empleado != null ) {
                found.setNombre( empleado.nombreCompleto() );
            }
            lstIngresos.add( found );
        }
        return found;
    }


    public List<Promocion> obtenerPromociones( Date fechaImpresion ) {

        Date fechaInicio;
        Date fechaFin;

        List<Promocion> lstPromociones = new ArrayList<Promocion>();
        List<Promocion> lstPromo = promocionRepository.findAll();

        for ( Promocion prom : lstPromo ) {
            fechaInicio = DateUtils.addDays( prom.getVigenciaIni(), -7 );
            fechaFin = DateUtils.addDays( prom.getVigenciaFin(), 7 );

            if ( fechaInicio.compareTo( fechaImpresion ) <= 0 && fechaFin.compareTo( fechaImpresion ) >= 0 ) {
                lstPromociones.add( prom );
            }
        }

        return lstPromociones;
    }


    public List<KardexPorArticulo> obtenerKardex( String article, Date fechaInicio, Date fechaFin ){
        QTransInv transInv = QTransInv.transInv;
        QTransInvDetalle transInvDet = QTransInvDetalle.transInvDetalle;
        List<TransInvDetalle> lstMovimientos = new ArrayList<TransInvDetalle>();
        List<TransInv> lstTransInvDate = ( List<TransInv> ) transInvRepository.findAll( transInv.fecha.between( fechaInicio, new Date() ), transInv.fechaMod.desc() );
        List<KardexPorArticulo> lstKardezSku = new ArrayList<KardexPorArticulo>();
        Articulo articulo = new Articulo();
        QArticulo art = QArticulo.articulo1;
        String [] articuloColor = article.split(",");
        String artl = articuloColor[0];
        String color = "";
        if(articuloColor.length > 1){
          color = articuloColor[1] != null ? articuloColor[1] : "" ;
        }
        BooleanBuilder booleanColor = new BooleanBuilder();
        if( color.trim().length() > 0 ){
          booleanColor.and(art.codigoColor.eq(color));
        } else {
          booleanColor.and(art.codigoColor.isEmpty()).or(art.codigoColor.isNull());
        }
        List<Articulo> articulos = (List<Articulo>) articuloRepository.findAll( art.articulo.trim().equalsIgnoreCase(artl.trim()).
                and(booleanColor) );
        if( articulos.size() == 1){
            articulo = articulos.get(0);
        }
        for( TransInv movimiento : lstTransInvDate ){
            TransInvDetalle transInvSku = ( TransInvDetalle ) transInvDetalleRepository.findOne( transInvDet.idTipoTrans.eq(movimiento.getIdTipoTrans()).
                    and( transInvDet.folio.eq( movimiento.getFolio() )).and( transInvDet.sku.eq(articulo.getId() != null ? articulo.getId() : 0) ) );
            if( transInvSku != null ){
                lstMovimientos.add( transInvSku );
            }
        }

        Integer exisActual = articulo.getCantExistencia() != null ? articulo.getCantExistencia() : 0;
        Integer saldoInicio = 0;
        Integer saldoFin = 0;
        for( TransInvDetalle movimiento : lstMovimientos ){
            Parametro parametro = parametroRepository.findOne( TipoParametro.ID_SUCURSAL.getValue() );
            Sucursal sucursal = sucursalRepository.findOne( Integer.parseInt(parametro.getValor()) );
            NotaVenta venta = notaVentaRepository.findOne( movimiento.getTransInv().getReferencia() );
            String factura;
            if( venta != null ){
                 factura = sucursal.getCentroCostos()+"-"+venta.getFactura();
            } else {
                factura = "-";
            }
            KardexPorArticulo kardexArticulo = new KardexPorArticulo( movimiento, factura );
            kardexArticulo.setFecha( movimiento.getTransInv().getFecha() );
            kardexArticulo.setFolio( movimiento.getFolio().toString() );
            kardexArticulo.setReferencia( factura );
            kardexArticulo.setTipoTransaccion( movimiento.getIdTipoTrans() );
            Empleado empleado = empleadoRepository.findById( movimiento.getTransInv().getIdEmpleado() );
            kardexArticulo.setEmpleado( empleado.getNombreCompleto() );
            if( movimiento.getTipoMov().equalsIgnoreCase( "S" )){
                if( lstMovimientos.get(0).equals(movimiento)){
                    saldoFin = exisActual;
                } else {
                    saldoFin = saldoInicio;
                }
                saldoInicio = saldoFin+movimiento.getCantidad();
                kardexArticulo.setSalida( movimiento.getCantidad() );
            } else if( movimiento.getTipoMov().equalsIgnoreCase( "E" ) ){
                if( lstMovimientos.get(0).equals(movimiento)){
                    saldoFin = exisActual;
                } else {
                    saldoFin = saldoInicio;
                }
                saldoInicio = saldoFin-movimiento.getCantidad();
                kardexArticulo.setEntrada( movimiento.getCantidad() );
            }
            kardexArticulo.setSaldoInicio( saldoInicio );
            kardexArticulo.setSaldoFinal( saldoFin );
            lstKardezSku.add( kardexArticulo );
        }
        return lstKardezSku;
    }


    public List<VentasPorDia> obtenerVentasDelDiaActual( Date fechaInicio, Date fechaFin, Boolean artPrecioMayorcero ){

        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and( nv.factura.isNotEmpty() ).and(nv.factura.isNotNull()), nv.factura.asc());
        List<VentasPorDia> lstVentasDia = new ArrayList<VentasPorDia>();
        for( NotaVenta venta : lstNotasVentas ){
            if( !TAG_CANCELADO.equalsIgnoreCase(venta.getsFactura()) ){
                VentasPorDia ventaDia = findorCreateFactura( lstVentasDia, venta.getFactura() );
                ventaDia.acumulaArticulos( venta, artPrecioMayorcero );
            }
        }

        return lstVentasDia;
    }



    public List<VentasPorDia> obtenerNotasDeCreditoEnVentasDelDiaActual( Date fechaInicio, Date fechaFin, Boolean artPrecioMayorcero ){

        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and( nv.factura.isNotEmpty() ).and(nv.factura.isNotNull()), nv.factura.asc());
        List<VentasPorDia> lstVentasDia = new ArrayList<VentasPorDia>();
        for( NotaVenta venta : lstNotasVentas ){
            if( !TAG_CANCELADO.equalsIgnoreCase(venta.getsFactura()) ){
                VentasPorDia ventaDia = findorCreateFactura( lstVentasDia, venta.getFactura() );
                ventaDia.acumulaNotasDeCredito( venta, artPrecioMayorcero );
            }
        }

        return lstVentasDia;
    }

    public List<VentasPorDia> obtenerCancelacionesDelDiaActual( Date fechaInicio, Date fechaFin, Boolean artPrecioMayorcero ){

        List<VentasPorDia> lstVentasDia = new ArrayList<VentasPorDia>();
        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion> ) modificacionRepository.findAll( modificacion.fecha.between(fechaInicio, fechaFin).
                and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)), modificacion.notaVenta.factura.asc() );

        for( Modificacion mod : lstCancelaciones ){
            VentasPorDia cancelacion = findorCreateFactura( lstVentasDia, mod.getNotaVenta().getFactura() );
            cancelacion.acumulaCancelaciones( mod.getNotaVenta(), mod, artPrecioMayorcero );
        }
        return lstVentasDia;
    }


    public List<VentasPorDia> obtenerVentasDelDiaActualPorGenerico( Date fechaInicio, Date fechaFin, Boolean artPrecioMayorCero ){

        List<VentasPorDia> lstVentasDia = new ArrayList<VentasPorDia>();
        BigDecimal notasCredito = BigDecimal.ZERO;
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and( nv.factura.isNotEmpty() ).and(nv.factura.isNotNull()), nv.factura.asc());
        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion> ) modificacionRepository.findAll( modificacion.fecha.between(fechaInicio, fechaFin).
                and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)) );

        for( NotaVenta nota : lstNotasVentas ){
            if( !TAG_CANCELADO.equalsIgnoreCase(nota.getsFactura()) ){
                for( DetalleNotaVenta det : nota.getDetalles() ){
                        VentasPorDia ventasGenericos = FindOrCreateGenerico( lstVentasDia, det.getArticulo().getGenerico().getDescripcion() );
                        ventasGenericos.acumulaArticulosPorgenericos( det, artPrecioMayorCero );
                    }
                }
        }

        for( Modificacion mod : lstCancelaciones ){
            List<DetalleNotaVenta> lstDet = new ArrayList<DetalleNotaVenta>(mod.getNotaVenta().getDetalles());
            Collections.sort( lstDet, new Comparator<DetalleNotaVenta>() {
            @Override
            public int compare( DetalleNotaVenta o1, DetalleNotaVenta o2 ) {
            return o1.getArticulo().getGenerico().getDescripcion().compareToIgnoreCase(o2.getArticulo().getGenerico().getDescripcion());
            }
            });
            for( DetalleNotaVenta det : lstDet ){
            VentasPorDia cancelacion = FindOrCreateGenerico( lstVentasDia, det.getArticulo().getGenerico().getDescripcion() );
            cancelacion.acumulaCancelacionesPorgenericos( det, artPrecioMayorCero );
            }
        }

        for( NotaVenta nota : lstNotasVentas ){
            if( !TAG_CANCELADO.equalsIgnoreCase(nota.getsFactura()) ){
                for( Pago pago : nota.getPagos()){
                    if( "NOT".equalsIgnoreCase(pago.getIdFPago()) ){
                        notasCredito = notasCredito.add(pago.getMonto());
                        for( DetalleNotaVenta det : nota.getDetalles() ){
                            VentasPorDia ventasGenericos = FindOrCreateGenerico( lstVentasDia, det.getArticulo().getGenerico().getDescripcion() );
                            ventasGenericos.acumulaNotasDeCreditoGenericos( det, pago.getMonto(), artPrecioMayorCero );
                        }
                    }
                }
            }
        }

        if( lstVentasDia.size() > 0 ){
            lstVentasDia.get(0).setMontoTotal( lstVentasDia.get(0).getMontoTotal().subtract(notasCredito) );
            lstVentasDia.get(0).setMontoConDescuento( lstVentasDia.get(0).getMontoConDescuento().subtract(notasCredito) );
        }

        return lstVentasDia;
    }

    protected VentasPorDia FindOrCreateGenerico( List<VentasPorDia> lstVemtas, String idGenerico ) {
        VentasPorDia found = null;

        for ( VentasPorDia ventas : lstVemtas ) {
            if ( ventas.getGenerico().equals( idGenerico ) ) {
                found = ventas;
                break;
            }
        }
        if ( found == null ) {
            found = new VentasPorDia( "", idGenerico, new Date() );
            lstVemtas.add( found );
        }
        return found;
    }


    public VentasPorDia findorCreateFactura( List<VentasPorDia> lstVentas, String factura ) {
        VentasPorDia found = null;
        for ( VentasPorDia ventas : lstVentas ) {
            if ( ventas.getFactura().equals( factura ) ) {
                found = ventas;
                break;
            }
        }
        if ( found == null ) {
            found = new VentasPorDia( factura, "", new Date() );
            lstVentas.add( found );
        }
        return found;
    }


    public List<IngresoPorDia> obtenerPagosPorPeriodo( Date fechaInicio, Date fechaFin ){
        List<IngresoPorDia> lstIngresos = new ArrayList<IngresoPorDia>();

        QNotaVenta nota = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nota.fechaHoraFactura.between(fechaInicio, fechaFin).
                and( nota.factura.isNotEmpty() ).and(nota.factura.isNotNull()), nota.fechaHoraFactura.asc() );
        for( NotaVenta notaVenta : lstNotasVentas ){
            List<Pago> lstPagos = new ArrayList<Pago>(notaVenta.getPagos());
            if( lstPagos.size() > 0 ){
                IngresoPorDia ingreso = FindOrCreate( lstIngresos, notaVenta.getFechaHoraFactura() );
                ingreso.AcumulaIngresosPorDia( notaVenta );
            }
        }

        return lstIngresos;
    }



    public List<VentasPorDia> obtenerVentasPorPeriodo( Date fechaInicio, Date fechaFin ){
        List<VentasPorDia> lstVentas = new ArrayList<VentasPorDia>();
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull()), nv.fechaHoraFactura.asc() );
        Parametro parametro = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        double iva = 1;
        try {
        iva = 1+(NumberFormat.getInstance().parse(parametro.getValor()).doubleValue()/100);
        } catch ( Exception e ){}

        for( NotaVenta nota : lstNotasVentas ){
            //if( !TAG_CANCELADO.equalsIgnoreCase(nota.getsFactura()) ){
                VentasPorDia ventas = FindOrCreatePorFecha( lstVentas, nota.getFechaHoraFactura() );
                ventas.acumulaVentasPorDia( nota, iva );
            //}
        }
        return lstVentas;
    }


    public List<VentasPorDia> obtenerVentasCanceladasPorPeriodo( Date fechaInicio, Date fechaFin ){
        List<VentasPorDia> lstVentas = new ArrayList<VentasPorDia>();
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull()), nv.fechaHoraFactura.asc() );
        QModificacion mod = QModificacion.modificacion;
        List<Modificacion> lstCancelaciones = ( List<Modificacion> ) modificacionRepository.findAll( mod.fecha.between(fechaInicio, fechaFin), mod.fecha.asc() );
        Parametro parametro = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        double iva = 1;
        try {
            iva = 1+(NumberFormat.getInstance().parse(parametro.getValor()).doubleValue()/100);
        } catch ( Exception e ){}

        for( Modificacion modificacion : lstCancelaciones ){
            VentasPorDia cancelaciones = FindOrCreatePorFecha( lstVentas, modificacion.getNotaVenta().getFechaHoraFactura() );
            cancelaciones.acumulaCancelacionesPorDia( modificacion, lstNotasVentas, iva );
        }

        return lstVentas;
    }

    public List<VentasPorDia> obtenerNotasDeCreditoEnVentasPorPeriodo( Date fechaInicio, Date fechaFin ){
        List<VentasPorDia> lstVentas = new ArrayList<VentasPorDia>();
        List<VentasPorDia> lstNotasCredito = new ArrayList<VentasPorDia>();
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio, fechaFin).
                and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull()), nv.fechaHoraFactura.asc() );
        Parametro parametro = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.getValue() );
        double iva = 1;
        try {
            iva = 1+(NumberFormat.getInstance().parse(parametro.getValor()).doubleValue()/100);
        } catch ( Exception e ){}

        for( NotaVenta nota : lstNotasVentas ){
            if( !TAG_CANCELADO.equalsIgnoreCase(nota.getsFactura()) ){
                VentasPorDia ventas = FindOrCreatePorFecha( lstVentas, nota.getFechaHoraFactura() );
                ventas.acumulaNotasCreditoVentasPorDia( nota, iva );
            }
        }

        for(VentasPorDia venta : lstVentas){
            if(venta.getEsNotaCredito()){
                lstNotasCredito.add( venta );
            }
        }
        return lstNotasCredito;
    }

    protected VentasPorDia FindOrCreatePorFecha( List<VentasPorDia> lstVentas, Date fecha ) {
        VentasPorDia found = null;

        for ( VentasPorDia ventas : lstVentas ) {
            if ( ventas.getFecha().equals( fecha ) ) {
                found = ventas;
                break;
            }
        }
        if ( found == null ) {
            found = new VentasPorDia( "", "", fecha );
            lstVentas.add( found );
        }
        return found;
    }


    public List<PromocionesAplicadas> obtenerPromocionesAplicadas( Date fechaInicio, Date fechaFin ){
        List<PromocionesAplicadas> lstPromociones = new ArrayList<PromocionesAplicadas>();

        QOrdenPromDet promocion = QOrdenPromDet.ordenPromDet;
        List<OrdenPromDet> lstPromos = ( List<OrdenPromDet> ) ordenPromDetRepository.findAll( promocion.fechaMod.between( fechaInicio, fechaFin ) );
        for(OrdenPromDet promo : lstPromos){
            NotaVenta nota = notaVentaRepository.findOne(promo.getIdFactura());
            Articulo articulo = articuloRepository.findOne(promo.getIdArticulo());
            QDetalleNotaVenta det = QDetalleNotaVenta.detalleNotaVenta;
            DetalleNotaVenta detalle = detalleNotaVentaRepository.findOne( det.idFactura.eq(nota.getId()).and(det.idArticulo.eq(articulo.getId())) );

            PromocionesAplicadas promocionAplicada = new PromocionesAplicadas();
            promocionAplicada.setFecha( nota.getFechaHoraFactura() );
            promocionAplicada.setFactura( nota.getFactura() );
            promocionAplicada.setIdArticulo( articulo.getId().toString() );
            promocionAplicada.setArticulo( articulo.getArticulo() );
            promocionAplicada.setImporteLista( detalle.getPrecioUnitLista() );
            promocionAplicada.setImporteDesc( promo.getDescuentoMonto() );
            promocionAplicada.setPorcentajeDesc( promo.getDescuentoPorcentaje().doubleValue() );
            promocionAplicada.setImporteTotal( detalle.getPrecioUnitFinal() );
            lstPromociones.add(promocionAplicada);
        }

        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasConDesc = ( List<NotaVenta> ) notaVentaRepository.findAll( nv.fechaHoraFactura.between(fechaInicio,fechaFin).
                and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull()).and(nv.montoDescuento.ne(BigDecimal.ZERO).and(nv.por100Descuento.ne(0))) );
        for(NotaVenta nota : lstNotasConDesc ){
            PromocionesAplicadas promocionAplicada = new PromocionesAplicadas();
            promocionAplicada.setFecha( nota.getFechaHoraFactura() );
            promocionAplicada.setFactura( nota.getFactura() );
            promocionAplicada.setIdArticulo( "-" );
            promocionAplicada.setArticulo( "-" );
            BigDecimal importeLista = BigDecimal.ZERO;
            BigDecimal importeTotal = BigDecimal.ZERO;
            for(DetalleNotaVenta det : nota.getDetalles() ){
                importeLista = importeLista.add(det.getPrecioUnitLista());
                importeTotal = importeTotal.add(det.getPrecioUnitFinal());
            }
            promocionAplicada.setImporteLista( importeLista );
            promocionAplicada.setImporteDesc( nota.getMontoDescuento() );
            promocionAplicada.setPorcentajeDesc( nota.getPor100Descuento().doubleValue() );
            promocionAplicada.setImporteTotal( importeTotal );
            lstPromociones.add(promocionAplicada);
        }

        return lstPromociones;
    }

    public List<ResumenCierre> obtenerVentasCierreDiario( Date fechaInicio, Date fechaFin ){
        List<ResumenCierre> lstIngresos = new ArrayList<ResumenCierre>();
        QNotaVenta nota = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotasVentas = ( List<NotaVenta> ) notaVentaRepository.findAll( nota.fechaHoraFactura.between(fechaInicio, fechaFin).
                and( nota.factura.isNotEmpty() ).and(nota.factura.isNotNull()).and(nota.sFactura.ne(TAG_CANCELADO)), nota.fechaHoraFactura.asc() );
        for( NotaVenta notaVenta : lstNotasVentas ){
            List<Pago> lstPagos = new ArrayList<Pago>(notaVenta.getPagos());
            if( lstPagos.size() > 0 ){
                ResumenCierre ingreso = FindOrCreateCierreDiario(lstIngresos, notaVenta.getFechaHoraFactura());
                ingreso.acumulaIngresosPorDia(notaVenta);
            }
        }

        return lstIngresos;

    }

    public List<ResumenCierre> obtenerDevolucionesCierreDiario( Date fechaInicio, Date fechaFin ){
        List<ResumenCierre> lstIngresos = new ArrayList<ResumenCierre>();
        List<Pago> lstPagos = new ArrayList<Pago>();
        QDevolucion dev = QDevolucion.devolucion;
        List<Devolucion> lstDevoluciones = ( List<Devolucion> ) devolucionRepository.findAll( dev.fecha.between( fechaInicio, fechaFin ).
                and( dev.tipo.eq( String.valueOf( "d" ) ) ), dev.idPago.asc() );
        for( Devolucion devolucion : lstDevoluciones ){
            ResumenCierre ingreso = FindOrCreateCierreDiario( lstIngresos, fechaInicio );
            ingreso.acumulaDevolucionesPorDia( devolucion );
        }
        Collections.sort( lstPagos, new Comparator<Pago>() {
            @Override
            public int compare(Pago o1, Pago o2) {
                return o1.getIdFactura().compareTo(o2.getIdFactura());
            }
        });

        return lstIngresos;
    }

    public ResumenCierre FindOrCreateCierreDiario( List<ResumenCierre> lstIngresos, Date fecha ) {
        Date onlyDay = DateUtils.truncate( fecha, Calendar.DATE );
        ResumenCierre found = null;
        for ( ResumenCierre ingresos : lstIngresos ) {
            if ( ingresos.getFecha().equals( onlyDay ) ) {

                found = ingresos;
                break;
            }
        }
        if ( found == null ) {
            found = new ResumenCierre( onlyDay );
            lstIngresos.add( found );
        }
        return found;
    }



    public List<VentasPorDia> obtenerVentasPorPeriodoMasVision( Date fechaInicio, Date fechaFin ){
        List<VentasPorDia> lstVentas = new ArrayList<VentasPorDia>();
        QNotaVenta nv = QNotaVenta.notaVenta;
        List<NotaVenta> lstNotas = (List<NotaVenta>) notaVentaRepository.findAll(
                nv.fechaHoraFactura.between(fechaInicio,fechaFin).and(nv.factura.isNotEmpty()).
                        and(nv.sFactura.ne(TAG_CANCELADO)), nv.factura.asc() );

        QModificacion modificacion = QModificacion.modificacion;
        List<Modificacion> lstCanceladas = (List<Modificacion>) modificacionRepository.findAll(modificacion.tipo.eq(TAG_TIPO_CANCELADO).
                and(modificacion.fecha.between(fechaInicio,fechaFin)).and(modificacion.notaVenta.fechaHoraFactura.notBetween(fechaInicio,fechaFin)));

        for( NotaVenta notas : lstNotas ){
            VentasPorDia ventas = findorCreateFactura( lstVentas, notas.getFactura() );
            ventas.acumulaVentasPorDiaMasVision( notas );
        }

        for( Modificacion mod : lstCanceladas){
            VentasPorDia cancelaciones = findorCreateFactura( lstVentas, mod.getNotaVenta().getFactura() );
            cancelaciones.acumulaCancelacionesPorDiaMasVision( mod );
        }
        return lstVentas;
    }



    public List<Descuento> obtenerDescuentosMasVision( Date fechaInicio, Date fechaFin, String key ) {
        BooleanBuilder claveBuilder = new BooleanBuilder();
        QDescuento descuento = QDescuento.descuento;
        if( !key.trim().equalsIgnoreCase("") ){
          claveBuilder.and( descuento.clave.eq(key) );
        } else {
          claveBuilder.and( descuento.idFactura.isNotNull() );
        }
        List<Descuento> lstDescuentos = ( List<Descuento> ) descuentoRepository.findAll( descuento.fecha.between( fechaInicio, fechaFin ).
                and(claveBuilder) );
        /*Integer noDesc = lstDescuento.size();
        for ( Descuento descuentos : lstDescuento ) {
            DescuentosPorTipo desc = FindOoorCreate( lstDescuentos, descuentos.getTipoClave() );
            desc.AcumulaDescuentos( descuentos, noDesc );
        }*/

        return lstDescuentos;
    }


    public List<Cotizaciones> obtenerCotizaciones( Date fechaInicio, Date fechaFin ) {
        List<Cotizaciones> lstCotizaciones = new ArrayList<Cotizaciones>();
        QCotizacion cotiza = QCotizacion.cotizacion;
        List<Cotizacion> cotizaciones = (List<Cotizacion>) cotizacionRepository.findAll( cotiza.fechaMod.between(fechaInicio,fechaFin) );

        for(Cotizacion cot : cotizaciones){
          List<Articulo> lstArticulos = new ArrayList<Articulo>();
          BigDecimal montoArticulos = BigDecimal.ZERO;
          Cliente cliente = clienteRepository.findOne( cot.getIdCliente() );
          NotaVenta nota = notaVentaRepository.findOne( cot.getIdFactura() != null ? cot.getIdFactura() : "" );
          Cotizaciones cotizacion = new Cotizaciones();
          cotizacion.setFechaMod( cot.getFechaMod() );
          cotizacion.setIdEmpleado(cot.getIdEmpleado());
          cotizacion.setIdCotizacion( cot.getIdCotiza().toString() );
          cotizacion.setCliente( cliente.getNombreCompleto() );
          if( !cot.getTel().trim().equalsIgnoreCase("") ){
            cotizacion.setContacto( cot.getTel() );
          } else{
            if(cliente != null && !cliente.getTelefonoCasa().trim().equalsIgnoreCase("")){
                cotizacion.setContacto( cliente.getTelefonoCasa().trim() );
            } else if(cliente != null && !cliente.getTelefonoAdicional().trim().equalsIgnoreCase("")){
                cotizacion.setContacto( cliente.getTelefonoAdicional().trim() );
            } else if(cliente != null && !cliente.getEmail().trim().equalsIgnoreCase("")){
                cotizacion.setContacto( cliente.getEmail().trim() );
            }
          }
          for(CotizaDet cotizaDet: cot.getCotizaDet()){
            Articulo articulo = articuloRepository.findOne( cotizaDet.getSku() );
            if(articulo != null){
              lstArticulos.add( articulo );
              List<Precio> precios = precioRepository.findByArticulo( articulo.getArticulo() );
              if(precios.size() > 0){
                montoArticulos = montoArticulos.add( precios.get(0).getPrecio() );
              }
            }
          }
          cotizacion.setLstArticulos( lstArticulos );
          cotizacion.setImporteTotal( montoArticulos );
          if( nota != null ){
            cotizacion.setFactura( nota.getFactura() );
          }
          cotizacion.setFechaVenta( cot.getFechaVenta() );

          lstCotizaciones.add( cotizacion );
        }

        return lstCotizaciones;
    }


  public List<VentasPorDia> obtenerVentasPorCupones( Date fechaInicio, Date fechaFin ){
    log.debug( "obtenerVentasPorCupones" );

    List<VentasPorDia> lstCupones = new ArrayList<VentasPorDia>();
    QPago payment = QPago.pago;
    List<Pago> lstPagos = (List<Pago>) pagoRepository.findAll( payment.fecha.between(fechaInicio,fechaFin).
            and(payment.idFPago.startsWith("C")).and(payment.notaVenta.sFactura.ne(TAG_CANCELADO)), payment.tipoPago.asc());


    for(Pago pago : lstPagos){
      VentasPorDia venta = findorCreateFactura(lstCupones, pago.getIdFPago());
      venta.acumulaCupones( pago );
    }

    return lstCupones;
  }



}
