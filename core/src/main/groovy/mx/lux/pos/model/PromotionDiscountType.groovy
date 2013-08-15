package mx.lux.pos.model

import org.apache.commons.lang3.StringUtils

enum PromotionDiscountType {
  StoreDiscount( "AG", "GERENCIA", "Descuento en Tienda" ),
  CorporateDiscount( "AP", "DIRECCION", "Descuento Corporativo" ),
   CouponDiscount("P","GERENCIA","Descuento por Cupon")
  
  private String idType
  private String description
  private String text
  
   PromotionDiscountType( pIdType, pDescription, pText ) {
    this.idType = StringUtils.trimToEmpty( pIdType ).toUpperCase( )
    this.description = StringUtils.trimToEmpty( pDescription ).toUpperCase( )
    this.text = StringUtils.trimToEmpty( pText )

  }
  
  String getIdType( ) {
    return this.idType
  }
  
  String getDescription( ) {
    return this.description
  }

  String getText( ) {
    return this.text
  }

  static PromotionDiscountType parse( String pIdType ) {
    String idtype = StringUtils.trimToEmpty( pIdType )
    PromotionDiscountType parsed = StoreDiscount
    for ( PromotionDiscountType pdt : PromotionDiscountType.values( ) ) {
      if ( idtype.equalsIgnoreCase( pdt.idType ) ) {
        parsed = pdt
        break
      }
    }
    return parsed
  }
  
}
