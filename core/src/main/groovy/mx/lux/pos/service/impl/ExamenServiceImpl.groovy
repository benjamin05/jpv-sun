package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Examen
import mx.lux.pos.repository.ExamenRepository
import mx.lux.pos.service.ExamenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

@Slf4j
@Service( "examenService" )
@Transactional( readOnly = true )
class ExamenServiceImpl implements ExamenService {

  @Resource
  private ExamenRepository examenRepository


  @Override
  @Transactional
  Examen guardarExamen( Examen examen ) {
    log.info( "guardando examen" )
      examenRepository.save( examen )
  }
}
