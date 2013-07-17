package mx.lux.pos.repository

import mx.lux.pos.model.Jb
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface JbRepository extends JpaRepository<Jb, String>, QueryDslPredicateExecutor<Jb> {

}
