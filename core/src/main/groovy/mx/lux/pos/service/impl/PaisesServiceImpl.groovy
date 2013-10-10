package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Paises
import mx.lux.pos.model.QPaises
import mx.lux.pos.repository.PaisesRepository
import mx.lux.pos.service.PaisesService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import java.text.Normalizer

@Slf4j
@Service( 'paisesService' )
@Transactional( readOnly = true )
class PaisesServiceImpl implements PaisesService {

  @Resource
  private PaisesRepository paisesRepository


  @Override
  List<Paises> obtenerPaises( ) {
    log.debug( "obteniendo paises" )
      QPaises paises = QPaises.paises
      return paisesRepository.findAll( paises.pais.isNotEmpty(), paises.orden.desc() )
  }

  @Override
  void guardarOrdenPais( String pais ){
      pais = pais.toUpperCase()
      String strStripped = null;
      pais = Normalizer.normalize(pais, Normalizer.Form.NFD);
      strStripped = pais.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

      QPaises paises = QPaises.paises
      Paises country = paisesRepository.findOne( paises.pais.equalsIgnoreCase(strStripped.trim()) )
      if( country != null ){
          paisesRepository.delete( country.id )
          paisesRepository.flush()
          country.orden = country.orden+1
          paisesRepository.save( country )
          paisesRepository.flush()
      } else {
          List<Paises> lstPaises = paisesRepository.findAll( paises.pais.isNotEmpty(), paises.id.asc() )
          country = new Paises()
          country.id = lstPaises.get( lstPaises.size()-1 ).id+1
          country.pais = pais.trim()
          country.orden = 1
          paisesRepository.save( country )
          paisesRepository.flush()
      }
  }

  void guardarPais( String pais ){
      if( StringUtils.trimToEmpty(pais).trim().length() > 0 ){
          pais = pais.toUpperCase()
          String strStripped = null;
          pais = Normalizer.normalize(pais, Normalizer.Form.NFD);
          strStripped = pais.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
          QPaises paises = QPaises.paises
          Paises country = paisesRepository.findOne( paises.pais.equalsIgnoreCase(strStripped.trim()) )
          if( country == null ){
              List<Paises> lstPaises = paisesRepository.findAll( paises.pais.isNotEmpty(), paises.id.asc() )
              country = new Paises()
              country.id = lstPaises.get( lstPaises.size()-1 ).id+1
              country.pais = strStripped.trim()
              country.orden = 0
              paisesRepository.save( country )
              paisesRepository.flush()
          }
      }
  }

}