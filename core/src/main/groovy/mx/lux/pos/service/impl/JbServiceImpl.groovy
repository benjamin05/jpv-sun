package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Jb
import mx.lux.pos.model.JbTrack
import mx.lux.pos.model.QJb
import mx.lux.pos.repository.JbRepository
import mx.lux.pos.repository.JbTrackRepository
import mx.lux.pos.service.JbService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

@Slf4j
@Service( 'JbService' )
@Transactional( readOnly = true )
class JbServiceImpl implements JbService {

  @Resource
  private JbRepository jbRepository

    @Override
    Jb findJBbyRx(String rx) {
        return jbRepository.findOne(rx)
    }
}
