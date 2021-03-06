package mx.lux.pos.service

import mx.lux.pos.model.Articulo
import mx.lux.pos.model.ArticuloSombra
import mx.lux.pos.model.Diferencia
import mx.lux.pos.model.Generico
import mx.lux.pos.model.InventarioFisico
import mx.lux.pos.model.MontoGarantia

interface ArticuloService {

  Articulo obtenerArticulo( Integer id )

  Articulo obtenerArticulo( Integer id, boolean incluyePrecio )

  List<Articulo> listarArticulosPorCodigo( String articulo )

  List<Articulo> listarArticulosPorCodigo( String articulo, boolean incluyePrecio )

  List<Articulo> listarArticulosPorCodigoSimilar( String articulo )

  List<Articulo> listarArticulosPorCodigoSimilar( String articulo, boolean incluyePrecio )

  Integer obtenerExistencia( Integer id )

  Boolean validarArticulo( Integer id )

  String validarGenericoArticulo( Integer id )

  Boolean registrarArticulo( Articulo pArticulo )

  Boolean registrarListaArticulos( List<Articulo> pListaArticulo )

  Boolean esInventariable( Integer id )

  List<Articulo> obtenerListaArticulosPorId( List<Integer> pListaId )

  Boolean actualizarArticulosConSombra( Collection<ArticuloSombra> pShadowSet )

  Collection<Generico> listarGenericos( Collection<String> pIdGenericoSet )

  List<Articulo> findArticuloyColor( String articulo, String color )

  String obtenerListaGenericosPrecioVariable( )

  Boolean useShortItemDescription( )

  Boolean generarArchivoInventario( )

  Boolean enviarInventario( )

  Boolean recibeDiferencias( )

  List<Diferencia> obtenerDiferencias(  )

  Boolean generarArchivoInventarioFisico( )

  Articulo buscaArticuloMenorPrecio( List<Integer> lstArticulo )

  Boolean tienenArticuloMsimoPrecio( List<Integer> lstArticulo )

  Boolean generaDiferencias( List<InventarioFisico> lstInventarioFisico )

  Boolean cargaDiferencias( )

  List<InventarioFisico> cargaArchivoInventarioFisico()

  Boolean inicializarInventario( )

  void difArticulosNoInv()

  Boolean generarArchivoDiferencias( )

  Articulo buscaArticulo( Integer id )

  MontoGarantia obtenerMontoGarantia( BigDecimal precioArt )
}
