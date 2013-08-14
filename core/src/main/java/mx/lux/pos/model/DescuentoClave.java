package mx.lux.pos.model;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Cacheable
@Table( name = "descuentos_clave", schema = "public" )
public class DescuentoClave implements Serializable {


    @Id
    @Column( name = "clave_descuento" )
    private Integer clave_descuento;

    @Column( name = "porcenaje_descuento")
    private Double porcenaje_descuento;

    @Column( name = "descripcion_descuento")
    private String descripcion_descuento;


    public Integer getClave_descuento() {
        return clave_descuento;
    }

    public void setClave_descuento(Integer clave_descuento) {
        this.clave_descuento = clave_descuento;
    }

    public Double getPorcenaje_descuento() {
        return porcenaje_descuento;
    }

    public void setPorcenaje_descuento(Double porcenaje_descuento) {
        this.porcenaje_descuento = porcenaje_descuento;
    }

    public String getDescripcion_descuento() {
        return descripcion_descuento;
    }

    public void setDescripcion_descuento(String descripcion_descuento) {
        this.descripcion_descuento = descripcion_descuento;
    }
}
