package mx.lux.pos.service.impl

import mx.lux.pos.model.GrupoArticuloDet
import mx.lux.pos.model.Parametro
import mx.lux.pos.model.PromotionAvailable
import mx.lux.pos.model.PromotionModel
import mx.lux.pos.model.MensajeTicket
import mx.lux.pos.model.QPromocion
import mx.lux.pos.model.TipoParametro
import mx.lux.pos.repository.GrupoArticuloDetRepository
import mx.lux.pos.repository.MensajeTicketRepository
import mx.lux.pos.repository.ParametroRepository
import mx.lux.pos.repository.PromocionRepository
import mx.lux.pos.repository.SucursalRepository
import mx.lux.pos.service.PromotionService
import mx.lux.pos.service.io.PromotionsAdapter
import mx.lux.pos.model.Promocion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import mx.lux.pos.service.business.*
import mx.lux.pos.model.PromotionDiscount

import java.text.NumberFormat
import java.text.SimpleDateFormat

@Service( 'promotionService' )
@Transactional( readOnly = true )
class PromotionServiceImpl implements PromotionService {

  private static Logger log = LoggerFactory.getLogger( PromotionServiceImpl.class )

  private Map<String, PromotionsAdapter> prData = new TreeMap<String, PromotionsAdapter>()

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private PromocionRepository promocionRepository

  @Resource
  private SucursalRepository sucursalRepository

  @Resource
  private MensajeTicketRepository mensajeTicketRepository

  @Resource
  private GrupoArticuloDetRepository grupoArticuloDetRepository


  public void updateOrder( PromotionModel pModel, String pOrderNbr ) {
    log.debug( "Update Order: ${ pOrderNbr } " )
    PromotionEngine.instance.updateOrder( pModel, pOrderNbr )
  }

  @Transactional
  Boolean requestApplyPromotion( PromotionModel pModel, PromotionAvailable pPromotion ) {
    log.debug( "Apply Promotion: ${ pPromotion.description } " )
    return PromotionEngine.instance.applyPromotion( pModel, pPromotion )
  }

  @Transactional
  Boolean requestCancelPromotion( PromotionModel pModel, PromotionAvailable pPromotion ) {
    log.debug( "Cancel Promotion: ${ pPromotion.description } " )
    return PromotionEngine.instance.cancelPromotion( pModel, pPromotion, true )
  }

  @Transactional
  Boolean requestCancelPromotionDiscount( PromotionModel pModel, PromotionDiscount pPromotion ) {
      log.debug( "Cancel Promotion: ${ pPromotion.description } " )
      return PromotionEngine.instance.cancelPromotionDiscount( pModel, pPromotion, true )
  }

  Boolean requestOrderDiscount( PromotionModel pModel, String pCorporateKey, Double pDiscountPercent, Boolean warranty ) {
    log.debug( String.format( "Request Order Discount (Key:%s, Discount:%,.1f%%)",
        pCorporateKey, ( pDiscountPercent * 100.0 ) ) )
    return PromotionEngine.instance.applyOrderDiscount( pModel, pCorporateKey, pDiscountPercent, warranty )
  }

  void requestPersist( PromotionModel pModel ) {
    log.debug( String.format( "Request Persist Promotions for Order:%s", pModel.order.orderNbr ) )
    PromotionCommit.writePromotions( pModel )
    PromotionCommit.writeDiscounts( pModel )
  }

  Double requestTopStoreDiscount( ) {
    Double discount = PromotionQuery.getTopStoreDiscount()
    log.debug( String.format( "Request Top Discount in Store: %,.1f%%", discount * 100.0 ) )
    return discount
  }

  Boolean requestVerify( String pCorporateKey, Double pDiscountPct ) {
    log.debug( String.format( "RequestVerify( %s, %,.1f%%)", pCorporateKey, pDiscountPct ) )
    return PromotionEngine.instance.verifyCorporateKey( pCorporateKey, pDiscountPct )
  }

  @Override
  String obtenRutaPorRecibir( ) {
    log.debug( "obteniendo ruta por recibir" )
    def parametro = parametroRepository.findOne( TipoParametro.RUTA_POR_RECIBIR.value )
    log.debug( "ruta por recibir: ${parametro?.valor}" )
    return parametro?.valor
  }

  @Override
  String obtenRutaRecibidos( ) {
    log.debug( "obteniendo ruta recibidos" )
    def parametro = parametroRepository.findOne( TipoParametro.RUTA_RECIBIDOS.value )
    log.debug( "ruta recibidos: ${parametro?.valor}" )
    return parametro?.valor
  }


  @Override
  void RegistrarPromociones( ) {
    log.debug( "RegistrarPromociones()" )
    try {
      Parametro ubicacion = Registry.find( TipoParametro.RUTA_POR_RECIBIR )
      log.debug( "Ubicacion:: %s", ubicacion.valor )
      Parametro parametro = parametroRepository.findOne( TipoParametro.RUTA_RECIBIDOS.value )
      PromotionImportTask promotionImportTask = new PromotionImportTask()

      List<String> lstGrupoPromociones = promotionImportTask.runGroupPromotions( ubicacion.valor, parametro.valor )
      if ( lstGrupoPromociones.size() > 0 ) {
        PromotionCommit.updateGroupPromotions( lstGrupoPromociones )
        log.debug( "Se registraron los grupos de promociones" )
      }

      List<PromotionsAdapter> lstPromociones = promotionImportTask.run( ubicacion.valor, parametro.valor )
      log.debug( "Tamaño lista de Promociones::", lstPromociones.size() )

      PromotionCommit.updatePromotions( lstPromociones )
      log.debug( "Promociones Registradas" )
    } catch ( Exception e ) {
      log.error( "Error al registrar promociones: ", e )
    }
  }

  @Override
  Promocion obtenerPromocion( Integer idPromocion ){
    log.debug( "obtenerPromocion( Integer idPromocion )" )
    Promocion promocion = promocionRepository.findOne( idPromocion )
    return promocion
  }

  @Override
  Promocion obtenerPromocionByDescripcion( String descripcion ){
    log.debug( "obtener promocion por descripcion" )
    QPromocion promo = QPromocion.promocion
    Promocion promocion = promocionRepository.findOne( promo.descripcion.equalsIgnoreCase(descripcion.trim()) )
    return promocion
  }

  @Override
  Boolean esArticuloPromocionalAutomatico( ){
      return Registry.automaticPromotionalArticle
  }

  @Override
  String articulosGupoPromocion( Integer idGrupo ){
      String lstArticulos = ''
      List<GrupoArticuloDet> articulos = grupoArticuloDetRepository.findByIdGrupo( idGrupo )
      for(GrupoArticuloDet grupo : articulos){
          lstArticulos = lstArticulos+grupo.articulo+','
      }
      return lstArticulos
  }


  @Transactional
  @Override
  void cargaArchivoMensajeTicket( ){
    log.debug( "cargaArchivoMensajeTicket( )" )
    File folder = new File( Registry.inputFilePath )
    //File newFolder = new File( Registry.processedFilesPath )
    if( folder?.exists() ){
      folder.eachFileMatch( ~/.+_.+_.+_.+\.MSG/ ) { File file ->
        log.debug( "leyendo archivo: ${file.name}" )
        for(String line : file.readLines() ){
          line = line.replace( "||", "| |")
          line = line.replace( "||", "| |")
          String[] elementos = line.split( /\|/ )
          if( elementos.size() >= 7 ){
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy")
            MensajeTicket mensaje = new MensajeTicket()
            Integer folio = 0
            try{
              folio = NumberFormat.getInstance().parse(elementos[0])
            } catch (NumberFormatException e){ println e }
            mensaje.setFolio( folio )
            mensaje.setDescripcion( elementos[1] )
            mensaje.setFechaInicio( formatter.parse(elementos[2]) )
            mensaje.setFechaFinal( formatter.parse(elementos[3]) )
            mensaje.setIdLinea( elementos[4] )
            mensaje.setListaArticulo( elementos[5] )
            mensaje.setMensaje( elementos[6] )
            mensajeTicketRepository.saveAndFlush( mensaje )
          }
        }
        def newFile = new File( Registry.processedFilesPath, file.name )
        List<File> lstFiles = new ArrayList<>();
        if(newFile.exists()) {
          newFile.delete()
        }
        try {
          FileInputStream inFile = new FileInputStream(file);
          FileOutputStream outFile = new FileOutputStream(newFile);
          Integer c;
          lstFiles.add(file)
        while( (c = inFile.read() ) != -1)
          outFile.write(c);
          inFile.close();
          outFile.close();
        } catch(IOException e) {
          System.out.println( e )
        }
        for(File files : lstFiles){
          files.delete()
        }
      }
    }
  }
}