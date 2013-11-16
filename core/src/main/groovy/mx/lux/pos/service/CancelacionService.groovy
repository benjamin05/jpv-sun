package mx.lux.pos.service

import mx.lux.pos.model.*

interface CancelacionService {

  List<CausaCancelacion> listarCausasCancelacion( )

  boolean permitirCancelacionExtemporanea( String idNotaVenta )

  Modificacion registrarCancelacionDeNotaVenta( String idNotaVenta, Modificacion modificacion )

  List<Devolucion> listarDevolucionesDeNotaVenta( String idNotaVenta )

  List<Devolucion> registrarDevolucionesDeNotaVenta( String idNotaVenta, Map<Integer, String> devolucionesPagos )

  List<Pago> registrarTransferenciasParaNotaVenta( String idOrigen, String idDestino, Map<Integer, BigDecimal> transferenciasPagos )

  List<NotaVenta> listarNotasVentaOrigenDeNotaVenta( String idNotaVenta )

  BigDecimal obtenerCreditoDeNotaVenta( String idNotaVenta )

  void restablecerValoresDeCancelacion( String idNotaVenta )

  Boolean validandoTransferencia( String idNotaVenta )

  void restablecerMontoAlBorrarPago( Integer idPago )

  Boolean validandoEnvioPino( String idOrder )

  Jb actualizaJb( String idFactura )

  JbTrack insertaJbTrack( String idFactura )

  void eliminaJbLlamada( String idFactura )

  void generaAcuses( String idFactura )

  void actualizaGrupo( String idFactura, String trans )
}
