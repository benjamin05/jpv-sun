package mx.lux.pos.service.impl

import com.mysema.query.BooleanBuilder
import com.mysema.query.types.Predicate
import groovy.util.logging.Slf4j
import mx.lux.pos.repository.ArticuloRepository
import mx.lux.pos.repository.DiferenciaRepository
import mx.lux.pos.repository.MontoGarantiaRepository
import mx.lux.pos.repository.PrecioRepository
import mx.lux.pos.repository.SucursalRepository
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.ArticuloService
import mx.lux.pos.service.business.Registry
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import mx.lux.pos.model.*

import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import mx.lux.pos.util.CustomDateUtils
import org.springframework.ui.velocity.VelocityEngineUtils
import org.apache.velocity.app.VelocityEngine
import java.text.NumberFormat

@Slf4j
@Service( 'articuloService' )
@Transactional( readOnly = true )
class ArticuloServiceImpl implements ArticuloService {

  @Resource
  private ArticuloRepository articuloRepository

  @Resource
  private MontoGarantiaRepository montoGarantiaRepository

  @Resource
  private PrecioRepository precioRepository

  @Resource
  private DiferenciaRepository diferenciaRepository

  @Resource
  private VelocityEngine velocityEngine

  private static final Integer CANT_CARACTEREZ_SKU = 6
  private static final Integer CANT_CARACTEREZ_COD_BAR = 15
  private static final Integer CANT_ARTICULOS_ENVIAR = 500
  private static final String TAG_GENERICO_NO_INVENTARIABLE = "NO INVENTARIABLE"
  private static final String TAG_GENERICO_ARMAZON = "A"

  private Articulo establecerPrecio( Articulo articulo ) {
    // log.debug( "estableciendo precio para el articulo id: ${articulo?.id} articulo: ${articulo?.articulo}" )
    if ( articulo?.id ) {
      // log.debug( "obteniendo lista de precios" )
      List<Precio> precios = precioRepository.findByArticulo( articulo.articulo )
      if ( precios?.any() ) {
        Precio precioLista = precios.find { Precio tmp ->
          'L'.equalsIgnoreCase( tmp?.lista )
        }
        BigDecimal lista = precioLista?.precio ?: 0
        Precio precioOferta = precios.find { Precio tmp ->
          'O'.equalsIgnoreCase( tmp?.lista )
        }
        BigDecimal oferta = precioOferta?.precio ?: 0
        // log.debug( "precio lista valor: ${precioLista?.precio} id: ${precioLista?.id} lista: ${precioLista?.lista}" )
        // log.debug( "precio oferta valor: ${precioOferta?.precio} id: ${precioOferta?.id} lista: ${precioOferta?.lista}" )
        articulo.precio = oferta && ( oferta < lista ) ? oferta : lista
        articulo.precioO = oferta && ( oferta < lista ) ? oferta : 0
        // log.debug( "se establece precio ${articulo?.precio} para articulo id: ${articulo?.id}" )
      }
    }
    // log.debug( "Return articulo:: ${articulo.descripcion} " )
    return articulo
  }

  @Override
  Articulo obtenerArticulo( Integer id ) {
    return obtenerArticulo( id, true )
  }

  @Override
  Articulo obtenerArticulo( Integer id, boolean incluyePrecio ) {
    log.info( "obteniendo articulo con id: ${id} incluye precio: ${incluyePrecio}" )
    Articulo articulo = articuloRepository.findOne( id ?: 0 )
    if ( articulo?.id && incluyePrecio ) {
      return establecerPrecio( articulo )
    }
    return articulo
  }

  @Override
  List<Articulo> listarArticulosPorCodigo( String articulo ) {
    return listarArticulosPorCodigo( articulo, true )
  }

  @Override
  List<Articulo> listarArticulosPorCodigo( String articulo, boolean incluyePrecio ) {
    log.info( "listando articulos con articulo: ${articulo} incluye precio: ${incluyePrecio}" )
    Predicate predicate = QArticulo.articulo1.articulo.equalsIgnoreCase( articulo )
    List<Articulo> resultados = articuloRepository.findAll( predicate, QArticulo.articulo1.codigoColor.asc() ) as List<Articulo>
    if ( incluyePrecio ) {
      return resultados?.collect { Articulo tmp ->
        establecerPrecio( tmp )
      }
    }
    return resultados
  }

  @Override
  List<Articulo> listarArticulosPorCodigoSimilar( String articulo ) {
    return listarArticulosPorCodigoSimilar( articulo, true )
  }

  @Override
  List<Articulo> listarArticulosPorCodigoSimilar( String articulo, boolean incluyePrecio ) {
    log.info( "listando articulos con articulo similar: ${articulo}" )
    Predicate predicate = QArticulo.articulo1.articulo.startsWithIgnoreCase( articulo )
    List<Articulo> resultados = articuloRepository.findAll( predicate, QArticulo.articulo1.articulo.asc() ) as List<Articulo>
    if ( incluyePrecio ) {
      return resultados?.collect { Articulo tmp ->
        establecerPrecio( tmp )
      }
    }
    return resultados
  }

  @Override
  Integer obtenerExistencia( Integer id ) {
    Articulo articulo = obtenerArticulo( id, false )
    return articulo?.cantExistencia ?: 0
  }

  @Override
  Boolean validarArticulo( Integer id ) {
    return articuloRepository.exists( id )
  }

  @Override
  String validarGenericoArticulo( Integer id ) {
    String genericoInvalido = ""
    Articulo articulo = articuloRepository.findOne( id )
    if( articulo != null ){
      if( articulo.generico == null ){
        if(StringUtils.trimToEmpty(articulo.idGenerico).length() > 0){
          genericoInvalido =  StringUtils.trimToEmpty(articulo.idGenerico)
        } else if( articulo.idGenerico == null || StringUtils.trimToEmpty(articulo.idGenerico).length() <= 0 ){
          genericoInvalido =  "vacio"
        }
      } else if( !articulo.generico.inventariable ){
        genericoInvalido = TAG_GENERICO_NO_INVENTARIABLE
      }
    }
    return genericoInvalido
  }

  @Override
  @Transactional
  Boolean registrarArticulo( Articulo pArticulo ) {
    if ( pArticulo != null ) {
      pArticulo = articuloRepository.save( pArticulo )
      return pArticulo?.id > 0
    }
    return false
  }

  @Override
  @Transactional
  Boolean registrarListaArticulos( List<Articulo> pListaArticulo ) {
    boolean registrado = false
    if ( ( pListaArticulo != null ) && ( pListaArticulo.size() > 0 ) ) {
      articuloRepository.save( pListaArticulo )
      registrado = true
    }
    articuloRepository.flush()
    return registrado
  }

  @Override
  Boolean esInventariable( Integer id ) {
    boolean inventariable = false
    Articulo articulo = obtenerArticulo( id, false )
    if ( articulo != null ) {
      Generico genre = RepositoryFactory.genres.findOne( articulo.idGenerico )
      inventariable = genre?.inventariable
    }
    return inventariable
  }

  @Override
  List<Articulo> obtenerListaArticulosPorId( List<Integer> pListaId ) {
    return articuloRepository.findByIdIn( pListaId )
  }

  @Override
  Boolean actualizarArticulosConSombra( Collection<ArticuloSombra> pShadowSet ) {
    log.debug( String.format( "[Service] Actualizar articulos: %,d en lista", pShadowSet.size() ) )
    Boolean actualizado = false
    try {
      List<Articulo> updatedList = new ArrayList<Articulo>()
      for ( ArticuloSombra shadow in pShadowSet ) {
        Articulo part = articuloRepository.findOne( shadow.id_articulo )
        if ( part != null ) {
          shadow.updateArticulo( part )
        } else {
          if ( false ) {//if ( shadow.isValidForNew() ) {
            part = shadow.createArticulo()
          }
        }
        if ( part != null ) {
          updatedList.add( part )
        }
      }
      actualizado = registrarListaArticulos( updatedList )
    } catch ( Exception e ) {
      log.error( "[Service] ERROR! Actualizando articulos", e )
    }
    return actualizado
  }

  Collection<Generico> listarGenericos( Collection<String> pIdGenericoSet ) {
    log.debug( "Listar Genericos(%d Ids)", pIdGenericoSet.size() )
    Collection<Generico> lista = new ArrayList<Generico>()
    if ( pIdGenericoSet.size() > 0 ) {
      lista = RepositoryFactory.genres.findByIdIn( pIdGenericoSet )
    }
    return lista
  }

  List<Articulo> findArticuloyColor( String articulo, String color ) {
    log.debug( "findArticuloyColor()" )

    List<Articulo> lstArticulos = new ArrayList<Articulo>()
    List<Articulo> lstArticulos2 = new ArrayList<Articulo>()
    Integer idArticulo = 0

     if ( !articulo.contains( "-" ) && !articulo.contains( "/" ) && !articulo.contains( "+" ) && !articulo.contains( "." ) && articulo.isNumber() ) {
      try{
        if( articulo.length() > CANT_CARACTEREZ_COD_BAR ){
          articulo = articulo.substring( 1 )
        }
          if( articulo.length() > CANT_CARACTEREZ_SKU ){
            idArticulo = Integer.parseInt( articulo.substring( 0, CANT_CARACTEREZ_SKU ) )
          } else {
            idArticulo = Integer.parseInt( articulo )
          }
      }catch ( Exception e ){
        log.error( "No se introdujo el SKU del articulo", e  )
      }
    }

    QArticulo art = QArticulo.articulo1
    lstArticulos2 = articuloRepository.findAll( art.id.eq( idArticulo ).or( art.articulo.eq( articulo ) ) ) as List
    if ( lstArticulos2.size() == 0 || lstArticulos2.size() > 1 ) {
      log.debug( "if de Articulos" )
      BooleanBuilder colour = new BooleanBuilder()
      if ( color.length() == 0 ) {
        colour.and( art.codigoColor.isNull() )
      } else {
        colour.and( art.codigoColor.eq( color ) )
      }
      lstArticulos2 = articuloRepository.findAll( art.id.eq( idArticulo ).or( art.articulo.eq( articulo ) ).and( colour ) ) as List
    }
    if ( lstArticulos2.size() > 0 ) {
      lstArticulos = lstArticulos2
    }

    return lstArticulos
  }


  String obtenerListaGenericosPrecioVariable( ) {
    return Registry.getManualPriceTypeList()
  }

  Boolean useShortItemDescription( ) {
    return Registry.isShortDescription()
  }


  Boolean generarArchivoInventario( ){
    log.debug( "generarArchivoInventarioFisico( )" )
    Parametro ubicacion = Registry.find( TipoParametro.RUTA_POR_ENVIAR )
    Parametro sucursal = Registry.find( TipoParametro.ID_SUCURSAL )
    String nombreFichero = "${ String.format("%02d", NumberFormat.getInstance().parse(sucursal.valor)) }.${ CustomDateUtils.format( new Date(), 'dd-MM-yyyy' ) }.${ CustomDateUtils.format( new Date(), 'HHmm' ) }.inv"
    log.info( "Generando archivo ${ nombreFichero }" )
    QArticulo articulo = QArticulo.articulo1
    List<Articulo> lstArticulos = articuloRepository.findAll( articulo.cantExistencia.ne( 0 ).and(articulo.cantExistencia.isNotNull()), articulo.id.asc() )
    def datos = [
        articulos:lstArticulos
    ]
    Boolean generado = true
    try{
      String fichero = "${ ubicacion.valor }/${ nombreFichero }"
      log.debug( "Generando Fichero: ${ fichero }" )
      log.debug( "Plantilla: fichero-inv.vm" )
      File file = new File( fichero )
      if ( file.exists() ) { file.delete() } // Borramos el fichero si ya existe para crearlo de nuevo
      log.debug( 'Creando Writer' )
      FileWriter writer = new FileWriter( file )
      datos.writer = writer
      log.debug( 'Merge template' )
      VelocityEngineUtils.mergeTemplate( velocityEngine, "template/fichero-inv.vm", "ASCII", datos, writer )
      log.debug( 'Writer close' )
      writer.close()

    }catch(Exception e){
      log.error( "Error al generar archivo de inventario", e )
      generado = false
    }
    return generado
  }



  Boolean enviarInventario( ){
    log.debug("enviarInventario( )")
    DateFormat df = new SimpleDateFormat( "dd-MM-yyyy" )
    Integer idSuc = Registry.currentSite
    //String urlEnviaInv = Registry.URLSendInventory
    QArticulo articulo = QArticulo.articulo1
    List<Articulo> lstArticulos = articuloRepository.findAll( articulo.cantExistencia.ne( 0 ).and(articulo.cantExistencia.isNotNull()), articulo.id.asc() )
    String response = ''
    Integer noVecesEnviar = 0
    Integer cantArticulosEnviar = 0
    Integer num = lstArticulos.size()
    Integer sum = 0
    List<Integer> lstMultiplos = new ArrayList<>()
    for(int a=1; a <= num; a++){
        if(num%a == 0){
            lstMultiplos.add(a)
            sum = sum + a;
        }
    }
    noVecesEnviar = lstMultiplos.get(lstMultiplos.size()-2)
    cantArticulosEnviar = num/noVecesEnviar
    Integer contador = 0
    for(int i = 0; i <= noVecesEnviar; i++){
      String urlEnviaInv = Registry.URLSendInventory
      String valor = idSuc.toString().trim()+"|"+df.format(new Date())+"|"+i.toString()+'|'
      for(int j = 0; j < cantArticulosEnviar; j++){
        if(contador < num){
          valor = valor+lstArticulos.get(contador).id.toString().trim()+'>'+lstArticulos.get(contador).cantExistencia.toString().trim()+'|'
        }
        contador++
      }
      urlEnviaInv += String.format( '?arg=%s', URLEncoder.encode( String.format( '%s', valor ), 'UTF-8' ) )
      try{
          if(i <= noVecesEnviar){
          response = urlEnviaInv.toURL().text
          response = response?.find( /<XX>\s*(.*)\s*<\/XX>/ ) {m, r -> return r}
          }
      } catch ( Exception e ){
          println e
      }
    }
    log.debug(response)
    return response != null ? response.contains('FOLIO:') : false

  }



  Boolean recibeDiferencias( ){

    Integer idSuc = Registry.currentSite
    String urlRecibeDif = Registry.URLReceivedDifferences
    String response = ''
    try{
      urlRecibeDif += String.format( '?arg=%s', URLEncoder.encode( String.format( '%s', idSuc.toString().trim() ), 'UTF-8' ) )
      log.debug(urlRecibeDif)
      response = urlRecibeDif.toURL().text
      response = response?.find( /<XX>\s*(.*)\s*<\/XX>/ ) {m, r -> return r}
      log.debug( "resultado solicitud: ${response}" )
    } catch ( Exception e ){
        println e
    }
    String[] cadena = response != null ? response.split(/\|/) : ''
    if(cadena.length >= 1){
      diferenciaRepository.deleteAll()
      diferenciaRepository.flush()
      for(int i = 0;i < cadena.length;i++){
        String articulo = cadena[i]
        String[] descArticulo = articulo.split('>')
        Integer idArticulo = NumberFormat.getInstance().parse(descArticulo[0])
        Integer cantFisi = NumberFormat.getInstance().parse(descArticulo[1])
        Integer cantSoi = NumberFormat.getInstance().parse(descArticulo[2])
        Integer dif = NumberFormat.getInstance().parse(descArticulo[3])
        Diferencia diferencia = new Diferencia()
        diferencia.id = idArticulo
        diferencia.cantidadFisico = cantFisi
        diferencia.cantidadSoi = cantSoi
        diferencia.diferencias = dif
        diferenciaRepository.save( diferencia )
        diferenciaRepository.flush()
      }
    }

    return cadena.length > 0
  }


  @Override
  List<Diferencia> obtenerDiferencias(  ) {
    //return diferenciaRepository.findAll()
    QDiferencia qDiferencia = QDiferencia.diferencia
    /*List<Diferencia> lstDiferencias = diferenciaRepository.findAll( qDiferencia.diferencias.isNotNull().
          and(qDiferencia.diferencias.goe(1).or(qDiferencia.diferencias.loe(-1))), qDiferencia.id.asc() )
    return lstDiferencias*/
    return  diferenciaRepository.obtenerDiferencias( )
  }


    Boolean generarArchivoInventarioFisico( ){
        log.debug( "generarArchivoInventarioFisico( )" )
        Parametro ubicacion = Registry.find( TipoParametro.RUTA_POR_ENVIAR )
        Parametro sucursal = Registry.find( TipoParametro.ID_SUCURSAL )
        String nombreFichero = "${ String.format("%02d", NumberFormat.getInstance().parse(sucursal.valor)) }.${ CustomDateUtils.format( new Date(), 'dd-MM-yyyy' ) }.${ CustomDateUtils.format( new Date(), 'HHmm' ) }.invf"
        log.info( "Generando archivo ${ nombreFichero }" )
        QArticulo articulo = QArticulo.articulo1
        List<Articulo> lstArticulos = articuloRepository.findAll( articulo.cantExistencia.ne( 0 ).and(articulo.cantExistencia.isNotNull()), articulo.id.asc() )
        def datos = [
                articulos:lstArticulos
        ]
        Boolean generado = true
        try{
            String fichero = "${ ubicacion.valor }/${ nombreFichero }"
            log.debug( "Generando Fichero: ${ fichero }" )
            log.debug( "Plantilla: fichero-inv.vm" )
            File file = new File( fichero )
            if ( file.exists() ) { file.delete() } // Borramos el fichero si ya existe para crearlo de nuevo
            log.debug( 'Creando Writer' )
            FileWriter writer = new FileWriter( file )
            datos.writer = writer
            log.debug( 'Merge template' )
            VelocityEngineUtils.mergeTemplate( velocityEngine, "template/fichero-inv.vm", "ASCII", datos, writer )
            log.debug( 'Writer close' )
            writer.close()

        }catch(Exception e){
            log.error( "Error al generar archivo de inventario", e )
            generado = false
        }
        return generado
    }


    Articulo buscaArticuloMenorPrecio( List<Integer> lstArticulo ){
      Articulo articulo = new Articulo()
      Articulo articuloTmp = new Articulo()
      List<Articulo> lstArticulos = new ArrayList<>()
      for(Integer id : lstArticulo){
        lstArticulos.add(articuloRepository.findOne( id ))
      }
      if( lstArticulos.size() > 0 ){
        BigDecimal montoPrecio = BigDecimal.ZERO
        List<Precio> precio = precioRepository.findByArticulo( StringUtils.trimToEmpty(lstArticulos.get(0).articulo) )
        if( precio.size() > 0 ){
          montoPrecio = precio.get(0).precio
        } else {
          montoPrecio = lstArticulos.get(0).precio
        }
        articulo =  lstArticulos.get(0)
        articuloTmp = lstArticulos.get(0)
        for(int i=1;i<lstArticulos.size();i++){
          List<Precio> precioTmp = precioRepository.findByArticulo( StringUtils.trimToEmpty(lstArticulos.get(i).articulo) )
          BigDecimal montoPrecioTmp = BigDecimal.ZERO
          if( precioTmp.size() > 0 ){
            montoPrecioTmp = precioTmp.get(0).precio
          } else {
            montoPrecioTmp = lstArticulos.get(i).precio
          }
          if (montoPrecioTmp.compareTo(montoPrecio) <= 0){
              articulo = lstArticulos.get(i);
              montoPrecio = montoPrecioTmp
          }
        }
      }
      return articulo
    }

    Boolean generaDiferencias( List<InventarioFisico> lstInventarioFisico ){
      Boolean archivoCargado = false
      println "Cantidad de articulos en archivos: ${lstInventarioFisico.size()}"
      for(InventarioFisico inventario : lstInventarioFisico){
        Diferencia diferencia = diferenciaRepository.findOne( inventario.idArticulo )
        if( diferencia != null ){
          try {
            Integer cantidadFisico = diferencia.cantidadFisico != null ? diferencia.cantidadFisico : 0
            diferencia.cantidadFisico = cantidadFisico+inventario.cantidadFisico
            diferenciaRepository.actualizaCantFisico( diferencia.cantidadFisico, inventario.idArticulo )
            archivoCargado = true
          } catch ( Exception e ) {
            println e
            archivoCargado = false
          }
          /*Integer cantidadFisico = diferencia.cantidadFisico != null ? diferencia.cantidadFisico : 0
          diferencia.cantidadFisico = cantidadFisico+inventario.cantidadFisico
          diferencia.diferencias =  diferencia.cantidadSoi-inventario.cantidadFisico
          diferenciaRepository.save(diferencia)
          diferenciaRepository.flush()*/
        }
      }
      return archivoCargado
    }


    Boolean cargaDiferencias(  ) {
      Boolean cargado = false
      List<Diferencia> lstDiferencias = diferenciaRepository.obtenerDiferenciasPend()
      for(Diferencia dif : lstDiferencias){
        try{
          diferenciaRepository.calcularDiferencias( dif.id )
          cargado = true
        } catch ( SQLException e ){
          println dif.id
          println e
        }
      }
      if( lstDiferencias.size() <= 0 ){
        cargado = true
      }
      return cargado
    }


    List<InventarioFisico> cargaArchivoInventarioFisico(){
        File source = new File( Registry.physicalInventoryFilePath )
        File destination = new File( Registry.physicalInventoryFileReadPath )
        List<File> lstFiles = new ArrayList<>();
        List<InventarioFisico> lstInventarioFisico = new ArrayList<>()
        println source.exists()
        println destination.exists()
        if( source.exists() && destination.exists() ){
            source.eachFileMatch( ~/.+_.+_.+\.TXT/ ) { File file ->
                String[] archivoName = file.name.split("_")
                 //Integer.parseInt(mystring));
                String idSucursal = String.format("%02d", Registry.currentSite)
                if( archivoName[0].equalsIgnoreCase( idSucursal ) ){
                    file.eachLine { String line ->
                      if( StringUtils.trimToEmpty(line).length() > 0 ){
                        InventarioFisico inventarioFisico = new InventarioFisico()
                        String[] registro = line.split(/\|/)
                        Integer cantidad = 1
                        Integer idArticulo = 0
                        if( StringUtils.trimToEmpty(registro[1].toString()).length() >= 6 ){
                          try{
                            //cantidad = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(registro[0]))
                            idArticulo = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(registro[1]).substring(0,6))
                          } catch ( NumberFormatException e ) { println e }
                        } else {
                          println "Articulo menor a 6 digitos: "+registro[1]
                        }
                        Articulo articulo = articuloRepository.findOne( idArticulo )
                        if( articulo != null ){
                            inventarioFisico.idArticulo = articulo.id
                            inventarioFisico.cantidadFisico = cantidad
                            lstInventarioFisico.add( inventarioFisico )
                        }
                      } else {
                        println "Registro vacio"
                      }
                    }
                }
                File newFile = new File( destination, file.name )
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
            }
            source.eachFileMatch( ~/.+_.+_.+\.txt/ ) { File file ->
                String[] archivoName = file.name.split("_")
                //Integer.parseInt(mystring));
                String idSucursal = String.format("%02d", Registry.currentSite)
                if( archivoName[0].equalsIgnoreCase( idSucursal ) ){
                    file.eachLine { String line ->
                        if( StringUtils.trimToEmpty(line).length() > 0 ){
                            InventarioFisico inventarioFisico = new InventarioFisico()
                            String[] registro = line.split(/\|/)
                            Integer cantidad = 1
                            Integer idArticulo = 0
                            if( StringUtils.trimToEmpty(registro[1].toString()).length() >= 6 ){
                                try{
                                    //cantidad = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(registro[0]))
                                    idArticulo = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(registro[1]).substring(0,6))
                                } catch ( NumberFormatException e ) { println e }
                            } else {
                                println "Articulo menor a 6 digitos: "+registro[1]
                            }
                            Articulo articulo = articuloRepository.findOne( idArticulo )
                            if( articulo != null ){
                                inventarioFisico.idArticulo = articulo.id
                                inventarioFisico.cantidadFisico = cantidad
                                lstInventarioFisico.add( inventarioFisico )
                            }
                        } else {
                            println "Registro vacio"
                        }
                    }
                }
                File newFile = new File( destination, file.name )
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
            }
            for(File files : lstFiles){
                files.delete()
            }
        }
      return lstInventarioFisico
    }



    Boolean inicializarInventario( ){
      Boolean inicializado = false
      try{
        diferenciaRepository.limpiarTabla()
        diferenciaRepository.inicializarInventario()
        diferenciaRepository.inicializarInventarioExistNull()
        inicializado = true
      } catch ( Exception e ) {
        println e
        inicializado = false
      }
      return inicializado
    }


    @Override
    void difArticulosNoInv(){
      List<Diferencia> lstDifNoInv = diferenciaRepository.obtenerArtPend( )
      println "Cantiada articulos no inventario fisico: ${lstDifNoInv.size()}"
      for(Diferencia dif : lstDifNoInv){
        Integer diferencia = dif.cantidadSoi != null ? dif.cantidadSoi : 0
        diferenciaRepository.actualizaCantFisico(0,dif.id)
        diferenciaRepository.insertaDiferencias( diferencia, dif.id)
      }
      diferenciaRepository.insertaDiferenciasCero()
    }



    Boolean tienenArticuloMsimoPrecio( List<Integer> lstArticulo ){
      Boolean hasSamePrice = true
        Articulo articulo = new Articulo()
        Articulo articuloTmp = new Articulo()
        List<Articulo> lstArticulos = new ArrayList<>()
        for(Integer id : lstArticulo){
            lstArticulos.add(articuloRepository.findOne( id ))
        }
        if( lstArticulos.size() > 0 ){
            BigDecimal montoPrecio = BigDecimal.ZERO
            List<Precio> precio = precioRepository.findByArticulo( StringUtils.trimToEmpty(lstArticulos.get(0).articulo) )
            if( precio.size() > 0 ){
                montoPrecio = precio.get(0).precio
            } else {
                montoPrecio = lstArticulos.get(0).precio
            }
            articulo =  lstArticulos.get(0)
            articuloTmp = lstArticulos.get(0)
            for(int i=1;i<lstArticulos.size();i++){
                List<Precio> precioTmp = precioRepository.findByArticulo( StringUtils.trimToEmpty(lstArticulos.get(i).articulo) )
                BigDecimal montoPrecioTmp = BigDecimal.ZERO
                if( precioTmp.size() > 0 ){
                    montoPrecioTmp = precioTmp.get(0).precio
                } else {
                    montoPrecioTmp = lstArticulos.get(i).precio
                }
                if (montoPrecioTmp.compareTo(montoPrecio) < 0 || montoPrecioTmp.compareTo(montoPrecio) > 0){
                   hasSamePrice = false
                }
            }
        }
        return hasSamePrice
    }



  @Override
  Boolean generarArchivoDiferencias( ){
    log.debug( "Generando archivo de diferencias" )
    Runtime garbage = Runtime.getRuntime();
    garbage.gc();
    Boolean generated = false
    SucursalRepository sucRep = RepositoryFactory.siteRepository
    Sucursal sucursal = sucRep.findOne( Registry.currentSite )
    String centroCostos = sucursal != null ? StringUtils.trimToEmpty(sucursal.centroCostos) : StringUtils.trimToEmpty(Registry.currentSite.toString())
    File file = new File( "${Registry.diferencesPath}/${StringUtils.trimToEmpty(centroCostos)}_${new Date().format("ddMMyy")}_dif.TXT" )
    /*QDiferencia qDiferencia = QDiferencia.diferencia
      List<Diferencia> lstDiferencias = diferenciaRepository.findAll( qDiferencia.diferencias.isNotNull().
            and(qDiferencia.diferencias.goe(1).or(qDiferencia.diferencias.loe(-1))), qDiferencia.id.asc() )*/
      List<Diferencia> lstDiferencias = diferenciaRepository.obtenerDiferencias( )
      try{
          PrintStream strOut = new PrintStream( file )
          StringBuffer sb = new StringBuffer()
          //sb.append("${String.format("%02d",Registry.currentSite)}_${new Date().format("ddMMyy")}_dif.TXT")
          for(Diferencia dif : lstDiferencias){
              String marca = StringUtils.trimToEmpty(dif.articulo.marca)
              String articulo = StringUtils.trimToEmpty(dif.articulo.articulo)
              String descripcion = StringUtils.trimToEmpty(dif.articulo.descripcion)
              sb.append("${dif.id}|${marca}|${articulo}|${descripcion}|${dif.cantidadFisico}|${dif.cantidadSoi}|${dif.diferencias}|\n")
          }
          strOut.println sb.toString()
          strOut.close()
          generated = true
      } catch ( Exception e ) {
        println e
      }
     return generated
  }



  @Override
  Articulo buscaArticulo( Integer id ){
    return articuloRepository.findOne( id )
  }


  @Override
  MontoGarantia obtenerMontoGarantia( BigDecimal precioArt ){
    QMontoGarantia qMontoGarantia = QMontoGarantia.montoGarantia1
    return montoGarantiaRepository.findOne( qMontoGarantia.montoGarantia.eq(precioArt) )
  }


}
