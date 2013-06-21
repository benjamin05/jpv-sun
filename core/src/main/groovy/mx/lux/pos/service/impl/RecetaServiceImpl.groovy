package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Receta
import mx.lux.pos.repository.PagoRepository
import mx.lux.pos.service.RecetaService
import org.apache.commons.lang3.StringUtils
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
}
