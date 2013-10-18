package mx.lux.pos.service

import mx.lux.pos.model.*

interface NotaVentaService {

  NotaVenta obtenerNotaVenta( String idNotaVenta )

  NotaVenta abrirNotaVenta(String clienteID,String empleadoID )

  NotaVenta notaVentaxRx(Integer rx)

  NotaVenta registrarNotaVenta( NotaVenta notaVenta )

  NotaVenta registrarDetalleNotaVentaEnNotaVenta( String idNotaVenta, DetalleNotaVenta detalleNotaVenta )

  NotaVenta eliminarDetalleNotaVentaEnNotaVenta( String idNotaVenta, Integer idArticulo )

    Pago registrarPagoEnNotaVenta( String idNotaVenta, Pago pago )

  NotaVenta eliminarPagoEnNotaVenta( String idNotaVenta, Integer idPago )

  void eliminarNotaVenta( String idNotaVenta )

  NotaVenta cerrarNotaVenta( NotaVenta notaVenta )

  List<NotaVenta> listarUltimasNotasVenta( )

  List<NotaVenta> listarNotasVentaPorParametros( Map<String, Object> parametros )

  NotaVenta obtenerNotaVentaPorTicket( String ticket )

  SalesWithNoInventory obtenerConfigParaVentasSinInventario( )

  Empleado obtenerEmpleadoDeNotaVenta( pOrderId )

  void saveOrder( NotaVenta pNotaVenta )

  NotaVenta obtenerSiguienteNotaVenta( Integer pIdCustomer )

    void saveRx(NotaVenta rNotaVenta, Integer receta)

    void saveProDate(NotaVenta rNotaVenta, Date fechaPrometida)

    NotaVenta saveFrame(String idNotaVenta, String opciones, String forma)

  void validaSurtePorGenericoInventariable( NotaVenta notaVenta )

  void registraImpuestoPorFactura( NotaVenta notaVenta )

  Boolean ticketReusoValido( String ticket, Integer idArticulo )

  Boolean montoValidoFacturacion( String ticket )

  List<NotaVenta> obtenerDevolucionesPendientes( Date fecha )

  NotaVenta buscarNotasReuso( String idFactura )

  NotaVenta obtenerNotaVentaOrigen( String idNotaVenta )

}
