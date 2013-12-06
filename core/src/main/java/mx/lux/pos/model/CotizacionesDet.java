package mx.lux.pos.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CotizacionesDet {

    private Date fecha;
    private Integer idCotizacion;
    private String cliente;
    private String contacto;
    private List<Articulo> lstArticulos;
    private BigDecimal importeTotal;
    private String factura;
    private String articulos;


    public CotizacionesDet( Integer idCotizacion ) {
      this.idCotizacion = idCotizacion;
      idCotizacion = 0;
      lstArticulos = new ArrayList<Articulo>();
      importeTotal = BigDecimal.ZERO;
      factura = "";
    }

    public void AcumulaDetalles( Cotizacion cotizacion, List<Articulo> lstArticulos ){
      this.articulos = "";
      BigDecimal montoArticulos = BigDecimal.ZERO;
      fecha = cotizacion.getFechaMod();
      cliente = cotizacion.getNombre();
      contacto = cotizacion.getTel();
      for(Articulo articulo : lstArticulos){
        articulos = articulos+","+articulo.getArticulo().trim();
        montoArticulos = montoArticulos.add(articulo.getPrecio());
      }
      articulos = articulos.replaceFirst( ",","" );
      importeTotal = montoArticulos;
      factura = cotizacion.getIdFactura();
    }


    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Integer idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public List<Articulo> getLstArticulos() {
        return lstArticulos;
    }

    public void setLstArticulos(List<Articulo> lstArticulos) {
        this.lstArticulos = lstArticulos;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getArticulos() {
        return articulos;
    }

    public void setArticulos(String articulos) {
        this.articulos = articulos;
    }
}
