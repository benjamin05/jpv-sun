package mx.lux.pos.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DescuentosPorTipo {

    private String tipo;
    private String descTipoPago;
    private Integer totalDesc;
    private String idEmpleado;
    private String nombreEmpleado;
    private List<TipoDescuento> descuentos;
    private Integer total;
    private BigDecimal importeDolares;
    private Integer rxConVenta;
    private Integer rxSinVenta;

    public DescuentosPorTipo(String tipoDesc) {
        tipo = tipoDesc;
        descuentos = new ArrayList<TipoDescuento>();
        totalDesc = 0;
        total = 0;
        idEmpleado = tipoDesc;
        importeDolares = BigDecimal.ZERO;
        rxConVenta = 0;
        rxSinVenta = 0;
    }

    public void AcumulaDescuentos(@NotNull Descuento descuento, Integer contador) {
        totalDesc = contador;
        if (descuento.getNotaVenta() != null) {
            TipoDescuento descu = FindOrCreate(descuentos, descuento.getNotaVenta().getFactura());
            descu.AcumulaDescuento(descuento);
        }
    }

    public void AcumulaTipoPagos(@NotNull Pago pago, BancoEmisor banco, String descPago, Boolean esPagoDolares) {
        descTipoPago = descPago;
        TipoDescuento descu = FindOrCreate(descuentos, pago.getNotaVenta().getFactura());
        descu.AcumulaPago(pago, banco, esPagoDolares);
        if (esPagoDolares) {
            if (StringUtils.trimToEmpty(pago.getIdPlan()).length() > 0) {
                try {
                    importeDolares = importeDolares.add(new BigDecimal(NumberFormat.getInstance().parse(pago.getIdPlan()).doubleValue()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void AcumulaBanco(@NotNull BancoEmisor banco) {
        TipoDescuento descu = FindOrCreate(descuentos, banco.getDescripcion());
        descu.AcumulaBanco(banco);
    }

    public void AcumulaEmpleados(@NotNull Examen examen, Integer contador, @NotNull Receta receta) {
        Date fecha = DateUtils.truncate(receta.getFechaReceta(), Calendar.DAY_OF_MONTH);
        TipoDescuento descu = FindOrCreateDate(descuentos, receta.getIdCliente(), fecha );
        descu.AcumulaExamenes(examen, receta);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<TipoDescuento> getDescuentos() {
        return descuentos;
    }

    public void setDescuentos(List<TipoDescuento> descuentos) {
        this.descuentos = descuentos;
    }


    @Nullable
    protected TipoDescuento FindOrCreate(@NotNull List<TipoDescuento> lstDescuentos, String idFactura) {
        TipoDescuento found = null;

        for (TipoDescuento desc : lstDescuentos) {
            if (desc.getFactura().equalsIgnoreCase(idFactura)) {
                found = desc;
                break;
            }
        }
        if (found == null) {
            found = new TipoDescuento(idFactura);
            lstDescuentos.add(found);
        }
        return found;
    }


    @Nullable
    protected TipoDescuento FindOrCreateDate( @NotNull List<TipoDescuento> lstDescuentos, Integer idCliente, Date fecha ) {
        TipoDescuento found = null;

        for (TipoDescuento desc : lstDescuentos) {
            if (desc.getIdCliente().compareTo(idCliente) == 0 ) {
                found = desc;
                break;
            }
        }
        if (found == null) {
            found = new TipoDescuento(idCliente, fecha);
            lstDescuentos.add(found);
        }
        return found;
    }

    public Integer getTotalDesc() {
        return totalDesc;
    }

    public void setTotalDesc(Integer totalDesc) {
        this.totalDesc = totalDesc;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getDescTipoPago() {
        return descTipoPago;
    }

    public void setDescTipoPago(String descTipoPago) {
        this.descTipoPago = descTipoPago;
    }

    public BigDecimal getImporteDolares() {
        return importeDolares;
    }

    public void setImporteDolares(BigDecimal importeDolares) {
        this.importeDolares = importeDolares;
    }

    public Integer getRxConVenta() {
        return rxConVenta;
    }

    public void setRxConVenta(Integer rxConVenta) {
        this.rxConVenta = rxConVenta;
    }

    public Integer getRxSinVenta() {
        return rxSinVenta;
    }

    public void setRxSinVenta(Integer rxSinVenta) {
        this.rxSinVenta = rxSinVenta;
    }
}
