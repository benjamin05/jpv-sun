package mx.lux.pos.service.impl

import mx.lux.pos.model.FormaContacto



public interface FormaContactoService {

    FormaContacto findFCbyRx(String rx)

    FormaContacto saveFC (FormaContacto formaContacto)

    List<FormaContacto> findByidCliente( Integer idCliente )




}