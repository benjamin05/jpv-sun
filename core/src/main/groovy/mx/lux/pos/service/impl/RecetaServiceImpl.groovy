package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Acuse
import mx.lux.pos.model.DetalleNotaVenta
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.model.Parametro
import mx.lux.pos.model.Receta
import mx.lux.pos.model.TipoParametro
import mx.lux.pos.repository.AcuseRepository
import mx.lux.pos.repository.DetalleNotaVentaRepository
import mx.lux.pos.repository.NotaVentaRepository
import mx.lux.pos.repository.PagoRepository
import mx.lux.pos.repository.ParametroRepository
import mx.lux.pos.repository.ReimpresionRepository
import mx.lux.pos.service.NotaVentaService
import mx.lux.pos.service.RecetaService
import org.apache.commons.lang3.StringUtils
import org.hibernate.service.spi.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import mx.lux.pos.repository.RecetaRepository

@Slf4j
@Service( "recetaService" )
@Transactional( readOnly = true )
class RecetaServiceImpl implements RecetaService {

  @Resource
  private RecetaRepository recetaRepository

    @Resource
  private NotaVentaService notaVentaService

    @Resource
    private DetalleNotaVentaRepository detalleNotaVentaRepository

   @Resource
   private ParametroRepository parametroRepository

    @Resource
    private ReimpresionRepository reimpresionRepository

    @Resource
    private AcuseRepository acuseRepository

    @Override
  @Transactional
  Receta guardarReceta( Receta receta ) {

        log.info( "guardando receta con folio: ${receta.folio}" )
     try {
      receta = recetaRepository.save( receta )
      return receta
     }catch(ex){
         log.info(ex)
        return null
     }
  }

    @Override
    @Transactional
    Receta findbyId(Integer idRx){

        try  {
            Receta receta = recetaRepository.findById(idRx)
           return receta

        }catch(ex){
            return null
        }
    }

    @Override
    @Transactional
    List<Receta> recetaCliente (Integer IdCliente){
        List<Receta> recetas = recetaRepository.findByIdCliente(IdCliente)

    }

    @Override
    @Transactional
    void generaAcuse(String orderID){

       NotaVenta notaVenta = notaVentaService.obtenerNotaVenta(orderID)
       Receta rx = findbyId(notaVenta.receta)


        DetalleNotaVenta artArmazon = new DetalleNotaVenta()
        List<DetalleNotaVenta> articulos = detalleNotaVentaRepository.findByIdFactura(notaVenta?.id)
        Iterator iterator = articulos.iterator();
        while (iterator.hasNext()) {
            DetalleNotaVenta detalle = iterator.next()
            if(detalle?.articulo?.idGenerico.trim().equals('A')){
                artArmazon = detalle
            }
        }


        String trat = notaVenta?.udf2

        if(artArmazon?.surte.equals('P')){
            trat =trat+',SP'
        }

        Parametro reg_clases = parametroRepository.findOne('reg_clases')
        if(reg_clases.valor.equals('SI')){
           trat= trat + ',' +notaVenta?.cliente?.udf1
        }

        BigInteger primerTicket = reimpresionRepository.noReimpresiones(notaVenta?.factura).toInteger()



      String contenido = 'eje_dVal='+rx?.odEjeR
        contenido = contenido+'|cilindro_iVal='+rx?.oiCilR
        contenido = contenido+'|armazonVal='+artArmazon?.articulo?.articulo
        contenido = contenido+'|parVal=P'
        contenido = contenido+'|adicion_dVal='+rx?.odAdcR
        contenido = contenido+'|id_acuseVal='+acuseRepository.nextIdAcuse() + 1
        contenido = contenido+'|esfera_iVal='+ rx?.oiEsfR
        contenido = contenido+'|prisma_d_hVal='+ rx?.odPrismaH
        contenido = contenido+'|sucursalVal='+ notaVenta?.sucursal.id
        contenido = contenido+'|distancia_cVal='+rx?.diCercaR
        contenido = contenido+'|archivoVal='+notaVenta?.sucursal?.id.toString()+ notaVenta?.factura + primerTicket.toString()
        contenido = contenido+'|recetaVal='+notaVenta?.factura
        contenido = contenido+'|cilindro_dVal='+rx?.odCilR
        contenido = contenido+'|alturaVal='+ rx?.altOblR
        contenido = contenido+'|formaVal='+notaVenta?.udf3
        contenido = contenido+'|prisma_i_vVal='+rx?.oiPrismaV
        contenido = contenido+'|adicion_iVal='+  rx?.oiAdcR
        contenido = contenido+'|distancia_lVal='+ rx?.diLejosR
        contenido = contenido+'|tratamientosVal='+ trat
        contenido = contenido+'|observacionesVal='+ rx?.observacionesR+ ','+ notaVenta?.observacionesNv+',' + rx?.sUsoAnteojos
        contenido = contenido+'|uso='+ rx?.sUsoAnteojos
        contenido = contenido+'|prisma_d_vVal='+ rx?.odPrismaV
        contenido = contenido+'|distancia_m_iVal='+  rx?.diOi
        contenido = contenido+'|distancia_m_dVal='+  rx?.diOd
        contenido = contenido+'|prisma_i_hVal='+ rx?.oiPrismaH
        contenido = contenido+'|esfera_dVal='+rx?.odEsfR
        contenido = contenido+'|codigoVal='+ notaVenta.codigo_lente
        contenido = contenido+'|eje_iVal='+ rx?.oiEjeR
        contenido = contenido+'|'

        String cont = contenido
        contenido= ''
        for (int x=0; x < cont.length(); x++) {
            if (cont.charAt(x) != ' ')
                contenido += cont.charAt(x)
        }


        Acuse acuseRx = new Acuse()
        acuseRx.contenido = contenido
        acuseRx.idTipo = 'RX'
        acuseRx.fechaCarga = new Date()
        acuseRx.intentos = 0

        acuseRepository.saveAndFlush(acuseRx)

        generaArchivoEnvio(contenido, notaVenta?.sucursal?.id.toString()+ notaVenta?.factura + primerTicket.toString())


    }

    @Override
    private void generaArchivoEnvio( String contenido, String nombre) throws ServiceException {
            try {
                File archivo = new File( 'C:/jpv-sun/', nombre.toString() )
                BufferedWriter out = new BufferedWriter( new FileWriter( archivo ) )
                out.write( contenido )
                out.close()
            } catch ( Exception e ) {
                throw new ServiceException( "Error al generar archivo de envio externo", e )
            }

    }
}
