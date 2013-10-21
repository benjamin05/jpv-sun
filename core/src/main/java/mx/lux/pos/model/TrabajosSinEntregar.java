package mx.lux.pos.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrabajosSinEntregar {

    private String idEmpleado;

    private static final String TAG_CUPON = "C";

    public TrabajosSinEntregar(String idVendedor) {
        idEmpleado = idVendedor;

    }

   public void AcumulaPago( String idFactura,  BigDecimal monto, Date FechaPago ) {
        /*IngresoPorFactura ingreso = FindOrCreate( pagos, idFactura );
        ingreso.AcumulaPago( new BigDecimal(monto.doubleValue()), FechaPago );
        totalPagos = ( totalPagos.add( new BigDecimal(monto.doubleValue()) ) );*/
    }


}
