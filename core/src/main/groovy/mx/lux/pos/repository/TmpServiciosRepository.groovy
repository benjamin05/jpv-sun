package mx.lux.pos.repository

import mx.lux.pos.model.TmpServicios
import mx.lux.pos.repository.custom.TmpServiciosRepositoryCustom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor
import org.springframework.transaction.annotation.Transactional

@Transactional( readOnly = true )
interface TmpServiciosRepository extends JpaRepository<TmpServicios, Integer>, QueryDslPredicateExecutor<TmpServicios>, TmpServiciosRepositoryCustom {


}

