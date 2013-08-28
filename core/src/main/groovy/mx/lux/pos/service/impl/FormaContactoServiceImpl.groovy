package mx.lux.pos.service.impl

import com.mysema.query.jpa.JPQLQuery
import com.mysema.query.types.Predicate
import groovy.util.logging.Slf4j

import mx.lux.pos.model.FormaContacto

import mx.lux.pos.model.QFormaContacto
import mx.lux.pos.repository.FormaContactoRepository
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import javax.persistence.criteria.Expression

@Slf4j
@Service( 'FormaContactoService' )
@Transactional( readOnly = true )
class FormaContactoServiceImpl extends QueryDslRepositorySupport  implements FormaContactoService {

  @Resource
  private FormaContactoRepository formaContactoRepository


    @Override
    FormaContacto findFCbyRx(String rx) {
        return formaContactoRepository.findOne(rx)
    }

    @Override
    FormaContacto saveFC(FormaContacto formaContacto) {

        formaContacto = formaContactoRepository.saveAndFlush(formaContacto)

        return formaContacto
    }



    @Override
    List<FormaContacto> findByidCliente( Integer idCliente ) {
        QFormaContacto formaContacto = QFormaContacto.formaContacto
        def predicates = [ ]
        if (  idCliente != null ) {
            predicates.add( formaContacto.id_cliente.eq( idCliente ) )
        }
        JPQLQuery query = from( formaContacto )

        query.where( predicates as Predicate[] )
      //  query.groupBy(formaContacto?.contacto,formaContacto?.id_tipo_contacto)

        return query.list( formaContacto )

    }




}
