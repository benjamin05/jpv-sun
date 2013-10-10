package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QModificacion is a Querydsl query type for Modificacion
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QModificacion extends EntityPathBase<Modificacion> {

    private static final long serialVersionUID = 41829070;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QModificacion modificacion = new QModificacion("modificacion");

    public final StringPath causa = createString("causa");

    @NotNull
    public final QCausaCancelacion causaCancelacion;

    public final SetPath<Devolucion, QDevolucion> devolucion = this.<Devolucion, QDevolucion>createSet("devolucion", Devolucion.class, QDevolucion.class);

    @NotNull
    public final QEmpleado empleado;

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEmpleado = createString("idEmpleado");

    public final StringPath idFactura = createString("idFactura");

    @NotNull
    public final QModificacionCan modificacionCan;

    @NotNull
    public final QModificacionImp modificacionImp;

    @NotNull
    public final QModificacionPag modificacionPag;

    @NotNull
    public final QNotaVenta notaVenta;

    public final StringPath observaciones = createString("observaciones");

    public final StringPath tipo = createString("tipo");

    public QModificacion(String variable) {
        this(Modificacion.class, forVariable(variable), INITS);
    }

    public QModificacion(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QModificacion(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(Modificacion.class, metadata, inits);
    }

    public QModificacion(Class<? extends Modificacion> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.causaCancelacion = inits.isInitialized("causaCancelacion") ? new QCausaCancelacion(forProperty("causaCancelacion")) : null;
        this.empleado = inits.isInitialized("empleado") ? new QEmpleado(forProperty("empleado"), inits.get("empleado")) : null;
        this.modificacionCan = inits.isInitialized("modificacionCan") ? new QModificacionCan(forProperty("modificacionCan")) : null;
        this.modificacionImp = inits.isInitialized("modificacionImp") ? new QModificacionImp(forProperty("modificacionImp")) : null;
        this.modificacionPag = inits.isInitialized("modificacionPag") ? new QModificacionPag(forProperty("modificacionPag")) : null;
        this.notaVenta = inits.isInitialized("notaVenta") ? new QNotaVenta(forProperty("notaVenta"), inits.get("notaVenta")) : null;
    }

}

