package mx.lux.pos.service;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

public interface ReportService {

    @Nullable
    String obtenerReporteCierreDiario( Date fecha );

    @Nullable
    String obtenerReporteIngresosXSucursal( Date fechaInicio, Date fechaFin );



    @Nullable
    String obtenerReporteVentas( Date fechaInicio, Date fechaFin );


    @Nullable
    String obtenerReporteVentasporVendedorCompleto( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteTrabajosSinEntregar();

    @Nullable
    String obtenerReporteCancelacionesResumido( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteCancelacionesCompleto( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteVentasporLineaFactura( Date fechaInicio, Date fechaFin, String articulo, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteVentasporLineaArticulo( Date fechaInicio, Date fechaFin, String articulo, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteVentasMarca( Date fechaInicio, Date fechaFin, String marca, boolean noMostrarArticulos, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteVentasVendedorporMarca( Date fechaInicio, Date fechaFin, String marca, boolean mostrarArticulos, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteExistenciasporMarca( String marca, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteExistenciasporMarcaResumido( String marca, boolean gogle, boolean oftalmico, boolean todo );

    @Nullable
    String obtenerReporteExistenciasporArticulo( String marca, String descripcion, String color );

    @Nullable
    String obtenerReporteControldeTrabajos( boolean retenidos, boolean porEnviar, boolean pino, boolean sucursal, boolean todos, boolean factura, boolean fechaPromesa );

    @Nullable
    String obtenerReporteTrabajosEntregados( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteTrabajosEntregadosporEmpleado( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteVentasCompleto( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteFacturasFiscales( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteDescuentos( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReportePromocionesAplicadas( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReportePagos( Date fechaInicio, Date fechaFin, String fromaPago, String factura );

    @Nullable
    String obtenerReporteCotizaciones( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteExamenesResumido( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteExamenesCompleto( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteVentasporOptometrista( Date fechaInicio, Date fechaFin );

    @Nullable
    String obtenerReporteVentasporOptometristaResumido( Date fechaInicio, Date fechaFin, boolean todoTipo, boolean referido, boolean rx,
                                                        boolean lux, boolean todaVenta, boolean primera, boolean mayor, boolean resumen );

    @Nullable
    String obtenerReportePromociones( Date fechaImpresion );

    @Nullable
    String obtenerReporteDeKardex( String articulo, Date fechaInicio, Date fechaFin );

    @Nullable
    public String obtenerReporteDeVentasDelDiaActual( Date fechaVentas, Boolean artPrecioMayorCero );

    @Nullable
    public String obtenerReporteDeIngresosPorPeriodo( Date dateStart, Date dateEnd );

    @Nullable
    String obtenerReporteVentasMasVision( Date fechaInicio, Date fechaFin );

    @Nullable
    public String obtenerReporteDescuentosMasVision( Date fechaInicio, Date fechaFin, String key );

    @Nullable
    public String obtenerReporteDeCupones( Date dateStart, Date dateEnd );
}
