package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.JbTrack
import mx.lux.pos.repository.JbTrackRepository
import mx.lux.pos.service.JbTrackService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

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
