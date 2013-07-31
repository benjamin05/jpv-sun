package mx.lux.pos.service.impl

import com.mysema.query.BooleanBuilder
import com.mysema.query.types.Predicate
import groovy.util.logging.Slf4j
import mx.lux.pos.model.*
import mx.lux.pos.repository.ArticuloRepository
import mx.lux.pos.repository.JbTrackRepository
import mx.lux.pos.repository.PrecioRepository
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.ArticuloService
import mx.lux.pos.service.JbTrackService
import mx.lux.pos.service.business.Registry
import mx.lux.pos.util.CustomDateUtils
import org.apache.velocity.app.VelocityEngine
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.velocity.VelocityEngineUtils

import javax.annotation.Resource
import java.text.NumberFormat

@Slf4j
@Service( 'JbTrackService' )
@Transactional( readOnly = true )
class JbTrackServiceImpl implements JbTrackService {

  @Resource
  private JbTrackRepository jbTrackRepository

    @Override
    JbTrack saveJbTrack(JbTrack jbTrack) {
        jbTrack = jbTrackRepository.saveAndFlush(jbTrack)
      return jbTrack
    }
}
