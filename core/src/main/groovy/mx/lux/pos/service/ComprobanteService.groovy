package mx.lux.pos.service

import mx.lux.pos.model.Comprobante

interface ComprobanteService {

  Comprobante obtenerComprobante( String idFiscal )

  List<Comprobante> listarComprobantesPorTicket( String ticket )

  Comprobante registrarComprobante( Comprobante comprobante, Boolean desglosado )

  List<File> descargarArchivosComprobante( String idFiscal )

}
