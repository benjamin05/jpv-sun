package mx.lux.pos.service

import mx.lux.pos.model.Empleado
import mx.lux.pos.model.TipoContacto

interface ContactoService {

  List<TipoContacto> obtenerTiposContacto()

}
