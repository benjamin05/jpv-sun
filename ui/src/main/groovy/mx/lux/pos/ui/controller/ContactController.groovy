package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Cliente
import mx.lux.pos.model.Empleado
import mx.lux.pos.model.FormaContacto
import mx.lux.pos.model.Jb
import mx.lux.pos.model.TipoContacto
import mx.lux.pos.repository.TipoContactoRepository
import mx.lux.pos.service.ClienteService
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
  private static TipoContactoRepository tipoContactoRepository
  private static ClienteService clienteService

  @Autowired
  ContactController( JbService jbService, FormaContactoService formaContactoService, TipoContactoRepository tipoContactoRepository, ClienteService clienteService) {
    this.jbService = jbService
    this.formaContactoService = formaContactoService
    this.tipoContactoRepository = tipoContactoRepository
    this.clienteService = clienteService

  }

  static Jb findJbxRX( String rx ) {

    return jbService.findJBbyRx(rx)
  }

   static FormaContacto findFCbyRx(String rx){
     return formaContactoService.findFCbyRx(rx)
  }

    static List<FormaContacto> findCustomerContact(Integer idCliente){
        List<FormaContacto> contactos = new ArrayList<FormaContacto>()

        Cliente cliente = clienteService.obtenerCliente(idCliente)
            FormaContacto formaContacto = new FormaContacto()
            if (cliente.email != ''){
             formaContacto = new FormaContacto()
            formaContacto?.contacto = cliente.email
                TipoContacto tipoContacto = new TipoContacto()
                tipoContacto?.descripcion = 'Correo'
             formaContacto?.tipoContacto = tipoContacto
            contactos.add(formaContacto)
        }
        if(cliente.telefonoCasa != ''){
             formaContacto = new FormaContacto()
            formaContacto?.contacto = cliente.telefonoCasa
            TipoContacto tipoContacto = new TipoContacto()
            tipoContacto?.descripcion = 'Telefono'
            formaContacto?.tipoContacto = tipoContacto
            contactos.add(formaContacto)


        }

        if(cliente.telefonoTrabajo != ''){
             formaContacto = new FormaContacto()
            formaContacto?.contacto = cliente.telefonoTrabajo
            TipoContacto tipoContacto = new TipoContacto()
            tipoContacto?.descripcion = 'Recados'
            formaContacto?.tipoContacto = tipoContacto
            contactos.add(formaContacto)

        }

        if(cliente.telefonoAdicional != ''){
             formaContacto = new FormaContacto()
            formaContacto?.contacto = cliente.telefonoAdicional
            TipoContacto tipoContacto = new TipoContacto()
            tipoContacto?.descripcion = 'SMS'
            formaContacto?.tipoContacto = tipoContacto
            contactos.add(formaContacto)

        }



           println(contactos.get(0).contacto)
        println(contactos.get(0).tipoContacto.descripcion)

       return  contactos

    }

   static List<FormaContacto> findByIdCliente(Integer idCliente){
       List<FormaContacto> formaContactos = formaContactoService.findByidCliente(idCliente)
       List<FormaContacto> contactos = new ArrayList<FormaContacto>()
       Iterator iterator = formaContactos.iterator();
       while (iterator.hasNext()) {
           FormaContacto formaContacto = iterator.next()
             formaContacto?.tipoContacto =  tipoContactoRepository.findOne(formaContacto?.id_tipo_contacto)
            contactos.add(formaContacto)
       }
           return contactos
   }

   static FormaContacto saveFormaContacto(FormaContacto formaContacto){
      formaContacto = formaContactoService.saveFC(formaContacto)
       return formaContacto
   }



}