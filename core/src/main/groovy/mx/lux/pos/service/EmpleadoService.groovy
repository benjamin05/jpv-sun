package mx.lux.pos.service

import mx.lux.pos.model.Empleado

interface EmpleadoService {

  Empleado obtenerEmpleado( String id )

  Empleado gerente( )

  void actualizarPass( empleado )

}
