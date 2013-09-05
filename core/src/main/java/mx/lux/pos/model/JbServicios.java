package mx.lux.pos.model;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table( name = "jb_servicios", schema = "public" )
public class JbServicios implements Serializable {
    @Id
    @Column( name = "servicio" )
    private String servicio;

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }
}
