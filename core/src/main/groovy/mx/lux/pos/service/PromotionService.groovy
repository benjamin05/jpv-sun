package mx.lux.pos.service

import mx.lux.pos.model.PromotionAvailable
import mx.lux.pos.model.PromotionModel
import mx.lux.pos.model.Promocion
import mx.lux.pos.model.PromotionDiscount

interface PromotionService {

  void updateOrder( PromotionModel pModel, String pOrderNbr )

  Boolean requestApplyPromotion( PromotionModel pModel, PromotionAvailable pPromotion )

  Boolean requestCancelPromotion( PromotionModel pModel, PromotionAvailable pPromotion )

  Boolean requestCancelPromotionDiscount( PromotionModel pModel, PromotionDiscount pPromotion )

  Boolean requestOrderDiscount( PromotionModel pModel, String pCorporateKey, Double pDiscountPercent )

  void requestPersist( PromotionModel pModel )

    void saveTipoDescuento(String idNotaVenta, String idTipoDescuento )

  Double requestTopStoreDiscount( )

  Boolean requestVerify( String pCorporateKey, Double pDiscountPct )

  String obtenRutaPorRecibir( )

  String obtenRutaRecibidos( )

  void RegistrarPromociones()

  Promocion obtenerPromocion( Integer idPromocion )

}
