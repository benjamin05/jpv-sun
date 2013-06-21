package mx.lux.pos.repository

import org.springframework.data.jpa.repository.JpaRepository
import mx.lux.pos.model.ClienteProceso
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface ClienteProcesoRepository extends JpaRepository<ClienteProceso, Integer>,
                                   QueryDslPredicateExecutor<ClienteProceso> {

  List<ClienteProceso> findByEtapa( String pEtapa )

}

