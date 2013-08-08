package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Empleado
import mx.lux.pos.model.FormaContacto
import mx.lux.pos.model.Jb
import mx.lux.pos.service.EmpleadoService
import mx.lux.pos.service.JbService
import mx.lux.pos.service.SucursalService
import mx.lux.pos.service.impl.FormaContactoService
import mx.lux.pos.ui.model.Branch
import mx.lux.pos.ui.model.Session
import mx.lux.pos.ui.model.SessionItem
import mx.lux.pos.ui.model.User
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class ContactController {

  private static JbService jbService
  private static  FormaContactoService formaContactoService


  @Autowired
  ContactController( JbService jbService, FormaContactoService formaContactoService ) {
    this.jbService = jbService
    this.formaContactoService = formaContactoService

  }

  static Jb findJbxRX( String rx ) {

    return jbService.findJBbyRx(rx)
  }

   static FormaContacto findFCbyRx(String rx){
     return formaContactoService.findFCbyRx(rx)
  }

   static List<FormaContacto> findByIdCliente(Integer idCliente){
    return formaContactoService.findByidCliente(idCliente)
   }

   static FormaContacto saveFormaContacto(FormaContacto formaContacto){
      formaContacto = formaContactoService.saveFC(formaContacto)
       return formaContacto
   }

}
