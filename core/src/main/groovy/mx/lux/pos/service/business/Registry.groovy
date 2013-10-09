package mx.lux.pos.service.business

import mx.lux.pos.repository.impl.RepositoryFactory
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import mx.lux.pos.model.*
import java.text.NumberFormat

class Registry {


  private static final String TAG_TRANSACCION_VENTA = 'VENTA'
  private static final String TAG_TRANSACCION_REMESA = 'REM'
  static Parametro find( TipoParametro pParametro ) {
    Parametro p = RepositoryFactory.getRegistry().findOne( pParametro.getValue() )
    if ( p == null ) {
      p = new Parametro()
      p.id = pParametro.getValue()
      p.valor = pParametro.getDefaultValue()
      RepositoryFactory.getRegistry().saveAndFlush( p )
    }
    return p
  }

  static AcusesTipo findUrl( TipoUrl pUrl ) {
      AcusesTipo p = RepositoryFactory.getAcusesTipoRepository().findOne( pUrl.getValue() )
      if ( p == null ) {
          p = new AcusesTipo()
          p.id_tipo = pUrl.getValue()
          p.pagina = pUrl.getDefaultValue()
          RepositoryFactory.getAcusesTipoRepository().saveAndFlush( p )
      }
      return p
  }

  static Integer asInteger( TipoParametro pParametro ) {
    Integer num = 0
    Parametro p = find( pParametro )
    String value = StringUtils.trimToEmpty( p.valor )
    if ( value.length() > 0 ) {
      if ( NumberUtils.isNumber( p.valor ) ) {
        num = NumberFormat.getInstance().parse( p.valor )
      } else if ( NumberUtils.isNumber( pParametro.defaultValue ) ) {
        num = NumberUtils.createInteger( pParametro.defaultValue )
      }
    }
    return num
  }

  static Double asDouble( TipoParametro pParametro ) {
    Double d = 0
    Parametro p = find( pParametro )
    String value = StringUtils.trimToEmpty( p.valor )
    if ( value.length() > 0 ) {
      if ( NumberUtils.isNumber( p.valor ) ) {
        d = NumberUtils.createDouble( p.valor )
      } else if ( NumberUtils.isNumber( pParametro.defaultValue ) ) {
        d = NumberUtils.createDouble( pParametro.defaultValue )
      }
    }
    return d
  }

  static String asString( TipoParametro pParametro ) {
    Parametro p = find( pParametro )
    return StringUtils.trimToEmpty( p.valor )
  }

  static String asUrlString( TipoUrl pUrl ) {
      AcusesTipo p = findUrl( pUrl )
      return StringUtils.trimToEmpty( p.pagina )
    }

  static Boolean isFalse( TipoParametro pParametro ) {
    final String[] FALSE_VALUES = [ "no", "n", "false", "f", "off" ]
    Boolean b = false
    Parametro p = find( pParametro )
    String value = StringUtils.trimToEmpty( p.valor ).toLowerCase()
    if ( value.length() > 0 ) {
      for ( String falseValue : FALSE_VALUES ) {
        b = b || ( falseValue.equals( value ) )
        if ( b )
          break
      }
    }
    return b
  }

  static Boolean isTrue( TipoParametro pParametro ) {
    final String[] TRUE_VALUES = [ "si", "s", "yes", "y", "true", "t", "on" ]
    Boolean b = false
    Parametro p = find( pParametro )
    String value = StringUtils.trimToEmpty( p.valor ).toLowerCase()
    if ( value.length() > 0 ) {
      for ( String trueValue : TRUE_VALUES ) {
        b = b || trueValue.equals( value )
        if ( b )
          break
      }
    }
    return b
  }

  protected static TipoTransInv asTipoTransInv( TipoParametro pTipoTrans ) {
    String type = asString( pTipoTrans )
    TipoTransInv trType = InventorySearch.findTrType( type )
    if ( trType == null ) {
      trType = InventoryCommit.createTrType( pTipoTrans, type )
    }
    return trType
  }

  // Business Logic for Configuration Parameters
  static Boolean isExchangeDataFileRequired( ) {
    return isTrue( TipoParametro.INV_EXCHANGE_FILE_REQUIRED )
  }

  static Boolean isExportEnabledForInventory( String pTipoTransInv ) {
    Boolean enabled = false

    if ( InvTrType.SALES.equals( pTipoTransInv ) ) {
      enabled = isTrue( TipoParametro.INV_EXPORT_SALE_TR )
    } else if ( InvTrType.RECEIPT.equals( pTipoTransInv ) ) {
      enabled = isTrue( TipoParametro.INV_EXPORT_RECEIPT_TR )
    } else if ( InvTrType.ISSUE.equals( pTipoTransInv ) ) {
      enabled = isTrue( TipoParametro.INV_EXPORT_ISSUE_TR )
    } else if ( InvTrType.RETURN.equals( pTipoTransInv ) ) {
      enabled = isTrue( TipoParametro.INV_EXPORT_RETURN_TR )
    } else if ( InvTrType.ADJUST.equals( pTipoTransInv ) ) {
      enabled = isTrue( TipoParametro.INV_EXPORT_ADJUST_TR )
    } else if ( InvTrType.OUTBOUND.equals( pTipoTransInv ) ) {
        enabled = isTrue( TipoParametro.INV_EXPORT_SALIDA_ALMACEN_TR )
    } else if ( InvTrType.INBOUND.equals( pTipoTransInv ) ) {
        enabled = isTrue( TipoParametro.INV_EXPORT_ENTRADA_ALMACEN_TR )
    }
    return enabled
  }

  static FileFormat getCurrentFileFormat( ) {
    FileFormat format = FileFormat.DEFAULT
    String currentFormat = asString( TipoParametro.FORMATO_ARCHIVO )
    if ( FileFormat.LUX.equals( currentFormat ) ) {
      format = FileFormat.LUX
    } else if ( FileFormat.SUNGLASS.equals( currentFormat ) ) {
      format = FileFormat.SUNGLASS
    } else if ( FileFormat.MAS_VISION.equals( currentFormat ) ) {
      format = FileFormat.MAS_VISION
    }
    return format
  }

  static Double getCurrentVAT( ) {
    Double vat = 16.0
    String vatCode = asString( TipoParametro.IVA_VIGENTE )
    Impuesto vatRecord = RepositoryFactory.taxMaster.findOne( vatCode )
    if ( vatRecord != null ) {
      vat = vatRecord.tasa.doubleValue()
    }
    return vat
  }

  static String getCompanyShortName( ) {
    return asString( TipoParametro.COMPANIA_NOMBRE_CORTO )
  }

  static String getTipoPagoDolares( ){
    return asString( TipoParametro.TIPO_PAGO_DOLARES )
  }

  static String getTipoPagoCreditoEmpleado( ){
    return asString( TipoParametro.TIPO_PAGO_CRE_EMP )
  }

  static String getFechaPrimerArranque( ){
      return asString( TipoParametro.FECHA_PRIMER_ARRANQUE )
  }

  static Contribuyente getCompany( ) {

    Contribuyente company = null
    String rfc = asString( TipoParametro.COMPANIA_RFC )
    List<Contribuyente> companies = new ArrayList<Contribuyente>()
      companies.clear()
    if ( rfc.length() > 0 ) {
      Integer idCliente = RepositoryFactory.rfcMaster.getIdCliente(rfc.trim())

        if ( ( idCliente == null )) {
        companies = RepositoryFactory.rfcMaster.findByIdCliente( 0 )
      } else{
            companies = RepositoryFactory.rfcMaster.findByIdCliente( idCliente )
        }
      company = ( companies.size() > 0 ? companies.first() : null )
    }
    return company
  }

  static AddressAdapter getCompanyAddress( ) {
    return new AddressAdapter( getCompany() )
  }

  static Integer getCurrentSite( ) {
    return asInteger( TipoParametro.ID_SUCURSAL )
  }

  static String getActiveCustomers( ) {
    return asString( TipoParametro.CLIENTES_ACTIVOS )
  }

  static Boolean isUsdDisplayEnabled( ) {
    return isTrue( TipoParametro.DESPLIEGA_USD )
  }

  static Boolean isSunglass( ) {
    String companyGroup = asString( TipoParametro.GRUPO_COMPANIA ).toUpperCase()
    return companyGroup.startsWith( "S" )
  }

  static String getCatalogPath( ) {
    String ruta = asString( TipoParametro.RUTA_CATALOGOS )
    if ( isSunglass() ) {
      ruta = ruta + String.format( "%02d", getCurrentSite() )
    }
    return ruta
  }

  static String getInputFilePath( ) {
    return asString( TipoParametro.RUTA_POR_RECIBIR )
  }

  static String getPartMasterFile( ) {
    return getInputFilePath() + File.separator + getProductsFilePattern()
  }

  static String getProductsFilePattern( ) {
    return asString( TipoParametro.ARCHIVO_PRODUCTOS )
  }

  static String getClasificationFilePattern( ) {
      return asString( TipoParametro.ARCHIVO_CLASIFICACION_ARTICULOS )
  }

  static String getManualPriceTypeList( ) {
    return asString( TipoParametro.GENERICO_PRECIO_VARIABLE )
  }

  static Boolean isReceiptDuplicate( ) {
    return isTrue( TipoParametro.IMPRIME_DUPLICADO )
  }

  static String getDailyClosePath( ) {
    return asString( TipoParametro.RUTA_CIERRE )
  }

  static String isShortDescription( ) {
    return isTrue( TipoParametro.DESCRIPCION_CORTA )
  }

  static Boolean isFileFormatSunglass( ) {
    return FileFormat.SUNGLASS.equals( this.currentFileFormat )
  }

  static SalesWithNoInventory getConfigForSalesWithNoInventory( ) {
    SalesWithNoInventory result = SalesWithNoInventory.ALLOWED
    String autorizacion = asString( TipoParametro.VENTA_NEGATIVA_AUTORIZACION ).toUpperCase()
    if ( autorizacion.length() > 1 )
      autorizacion = autorizacion.substring( 0, 1 )
    for ( SalesWithNoInventory config : SalesWithNoInventory.values() ) {
      if ( config.value.equalsIgnoreCase( autorizacion ) ) {
        result = config
        break
      }
    }
    return result
  }

  static Cliente getGenericCustomer( ) {
    Cliente customer = RepositoryFactory.customerCatalog.findOne( asInteger( TipoParametro.ID_CLIENTE_GENERICO ) )
    return customer
  }

  static String getArchiveCommand( ) {
    return asString( TipoParametro.COMANDO_ZIP )
  }

  static String getArchivePath( ) {
    return asString( TipoParametro.RUTA_POR_ENVIAR )
  }

  static String getArchivePathDropbox( ) {
      return asString( TipoParametro.RUTA_POR_ENVIAR_DROPBOX )
  }

  static String getMessageFile( ) {
    return ( asString( TipoParametro.RUTA_POR_RECIBIR ) + File.separator + asString( TipoParametro.ARCHIVO_MENSAJE ) )
  }

  static String getProcessedFilesPath( ) {
    return asString( TipoParametro.RUTA_RECIBIDOS )
  }

  static String getEmployeeFilePattern( ) {
    return asString( TipoParametro.ARCHIVO_EMPLEADOS )
  }

  static String getFxRatesFilePattern( ) {
    return asString( TipoParametro.ARCHIVO_TIPO_CAMBIO )
  }

  static String getSiteSegment( ) {
    return asString( TipoParametro.COMPANIA_REGION )
  }

  static TipoTransInv getInvTrTypeAdjust( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_AJUSTE )
  }

  static TipoTransInv getInvTrTypeIssue( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_SALIDA )
  }

  static TipoTransInv getInvTrTypeReceipt( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_RECIBE_REMISION )
  }

  static TipoTransInv getInvTrTypeReturn( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_CANCELACION )
  }

  static TipoTransInv getInvTrTypeReturnXO( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_CANCELACION_EXTRA )
  }

  static TipoTransInv getInvTrTypeSale( ) {
    return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_VENTA )
  }

  static TipoTransInv getInvTrTypeSalidaAlmacen( ) {
     return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_SALIDA_ALMACEN )
  }

  static TipoTransInv getInvTrTypeEntradaAlmacen( ) {
     return asTipoTransInv( TipoParametro.TRANS_INV_TIPO_ENTRADA_ALMACEN )
  }
  static Boolean isCancellationLimitedToSameDay( ) {
    return isTrue( TipoParametro.CAN_MISMO_DIA )
  }

  static Integer getMaxLengthDescription( ) {
    return asInteger( TipoParametro.MAX_LONG_DESC_FACTURA )
  }

  static String getURLSalesNotification( ) {
    return asUrlString( TipoUrl.URL_ACUSE_VENTA_DIA )
  }

  static String getURLAdjustSalesNotification( ) {
    return asString( TipoParametro.URL_ACUSE_AJUSTE_VENTA )
  }

  static String getURLValidSP( ) {
      return asString( TipoParametro.VALIDA_SP )
  }

  static String getURLSalidaAlmacen( ) {
    return asString( TipoParametro.URL_SALIDA_ALMACEN )
  }

  static String getURLEntradaAlmacen( ) {
    return asUrlString( TipoUrl.URL_ACUSE_REMESA )
  }

  static String getURLConfirmaEntradaAlmacen() {
      return  asString(TipoParametro.URL_CONFIRMA_ENTRADA)
  }

    static String getAlmacenes() {
        return  asString(TipoParametro.ALMACENES)
    }

    static String getPackages() {
        return  asString(TipoParametro.PAQUETES)
    }

  static String getURL( String pAckType ) {
    String url = ''
    String type = StringUtils.trimToEmpty( pAckType ).toUpperCase( )
    if ( TAG_TRANSACCION_VENTA.equals(type) ) {
      url = getURLSalesNotification()
    } else if ( AckType.MODIF_VENTA.equals(type) ) {
      url = getURLAdjustSalesNotification()
    }  else if ( AckType.SALIDA_ALMACEN.equals(type) ) {
        url = getURLSalidaAlmacen()
    }  else if ( TAG_TRANSACCION_REMESA.equals(type) ) {
        url = getURLEntradaAlmacen()
    }
    return url
  }

    static String getURLConfirmacion( String pAckType ) {
        String url = ''
        String type = StringUtils.trimToEmpty( pAckType ).toUpperCase( )
        if ( AckType.ENTRADA_ALMACEN.equals(type) ) {
            url = getURLConfirmaEntradaAlmacen()
        }
        return url
    }
  static Double getAckDelay( ) {
    return asDouble( TipoParametro.ACUSE_RETRASO )
  }

  static Boolean isAckDebugEnabled() {
    return isTrue( TipoParametro.ACUSE_LOG_DETALLE )
  }

  static Double getAdvancePct() {
    return asDouble( TipoParametro.PORCENTAJE_ANTICIPO ) / 100.0
  }

  static Boolean isCardPaymentInDollars( String paymentType ){
    Boolean isPaymentDollar = false
    String[] pagos = tipoPagoDolares.split(',')
    for(int i=0; i == pagos.length; i++){
      if ( pagos[i].trim().contains( paymentType.trim() ) ) {
          isPaymentDollar = true
      }
    }
    return isPaymentDollar
  }

  private static String getInvAdjustPasswd( ) {
    return asString( TipoParametro.INVENTORY_ADJUST_PASSWORD )
  }

  static Boolean isInvAdjustPasswordValid( String pPasswd ) {
    Boolean valid = false
    if ( StringUtils.isNotBlank( pPasswd ) ) {
      valid = this.getInvAdjustPasswd().trim().equals( pPasswd.trim() )
    }
    return valid

  }

}
