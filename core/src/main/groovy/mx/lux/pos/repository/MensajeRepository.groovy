package mx.lux.pos.repository

import mx.lux.pos.model.Mensaje
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface MensajeRepository extends JpaRepository<Mensaje, Integer>, QueryDslPredicateExecutor<Mensaje> {

}
