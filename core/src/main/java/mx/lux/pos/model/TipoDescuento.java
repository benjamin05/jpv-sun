package mx.lux.pos.model;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

public class TipoDescuento {

    private String factura;
    private String idEmpleado;
    private String empleado;
    private String descuento;
    private String noConvenio;
    private Date fecha;
    private BigDecimal importe;
    private String claveP;
    private String refClave;
    private String idBancoEmi;
    private String idTerm;
    private String idPlan;
    private String f1;
    private String f2;
    private String f3;
    private String f4;
    private String f5;
    private String paciente;
    private Integer idCliente;
    private Integer rxConVenta;
    private Integer rxSinVenta;
    private final String DOLARES_RECIBIDOS = "USD Recibidos";

    public TipoDescuento(String idFactura) {
        factura = idFactura;
    }

    public TipoDescuento(Integer idCliente, Date fecha) {
        this.fecha = fecha;
        this.idCliente = idCliente;
        factura = "";
        rxConVenta = 0;
        rxSinVenta = 0;
    }

    public void AcumulaDescuento(Descuento descuentos) {
        idEmpleado = descuentos.getIdEmpleado();
        empleado = descuentos.getNotaVenta().getEmpleado().getNombreCompleto();
        descuento = descuentos.getPorcentaje();
        noConvenio = descuentos.getNotaVenta().getIdConvenio();
    }

    public void AcumulaPago(Pago pagos, BancoEmisor banco, Boolean esPagoDolares) {
        fecha = pagos.getFecha();
        importe = pagos.getMonto();
        claveP = pagos.geteTipoPago().getF1().equalsIgnoreCase(DOLARES_RECIBIDOS) ? "" : pagos.getClave();
        refClave = pagos.getReferenciaClave();
        idBancoEmi = pagos.getIdBancoEmisor();
        if (pagos.getTerminal() != null) {
            idTerm = pagos.getTerminal().getDescripcion();
        }
        idPlan = pagos.getIdPlan();
        f1 = pagos.geteTipoPago().getF1().equalsIgnoreCase(DOLARES_RECIBIDOS) ? "" : pagos.geteTipoPago().getF1();
        f2 = pagos.geteTipoPago().getF2();
        f3 = pagos.geteTipoPago().getF3();
        f4 = pagos.geteTipoPago().getF4();
        f5 = pagos.geteTipoPago().getF1().equalsIgnoreCase(DOLARES_RECIBIDOS) ? pagos.geteTipoPago().getF1() : pagos.geteTipoPago().getF5();
        if (esPagoDolares) {
            f5 = DOLARES_RECIBIDOS;
        }
    }

    public void AcumulaBanco(BancoEmisor banco) {
        idBancoEmi = banco.getDescripcion();
    }

    public void AcumulaExamenes(Examen examen, Receta receta) {
        fecha = examen.getFechaAlta();
        paciente = examen.getCliente().getNombreCompleto();
        if( receta.getNotaVenta() != null && receta.getNotaVenta().getFactura().length() > 0 ){
          factura = factura + ", " + StringUtils.trimToEmpty(receta.getNotaVenta() != null ? receta.getNotaVenta().getFactura() : "");
        }
        /*if( factura.trim().length() < 9 ){
          if( receta.getNotaVenta() != null && StringUtils.trimToEmpty(receta.getNotaVenta().getFactura()).length() > 0 ){
              rxConVenta = rxConVenta+1;
          } else {
              rxSinVenta = rxSinVenta+1;
          }
        }*/
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public String getDescuento() {
        return descuento;
    }

    public void setDescuento(String descuento) {
        this.descuento = descuento;
    }

    public String getNoConvenio() {
        return noConvenio;
    }

    public void setNoConvenio(String noConvenio) {
        this.noConvenio = noConvenio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getClaveP() {
        return claveP;
    }

    public void setClaveP(String claveP) {
        this.claveP = claveP;
    }

    public String getRefClave() {
        return refClave;
    }

    public void setRefClave(String refClave) {
        this.refClave = refClave;
    }

    public String getIdBancoEmi() {
        return idBancoEmi;
    }

    public void setIdBancoEmi(String idBancoEmi) {
        this.idBancoEmi = idBancoEmi;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    public String getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(String idPlan) {
        this.idPlan = idPlan;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getF3() {
        return f3;
    }

    public void setF3(String f3) {
        this.f3 = f3;
    }

    public String getF4() {
        return f4;
    }

    public void setF4(String f4) {
        this.f4 = f4;
    }

    public String getF5() {
        return f5;
    }

    public void setF5(String f5) {
        this.f5 = f5;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
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
