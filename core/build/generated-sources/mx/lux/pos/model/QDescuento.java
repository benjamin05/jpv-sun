package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QDescuento is a Querydsl query type for Descuento
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDescuento extends EntityPathBase<Descuento> {

    private static final long serialVersionUID = 1042363143;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QDescuento descuento = new QDescuento("descuento");

    public final StringPath clave = createString("clave");

    @NotNull
    public final QDescuentoClave descuentosClave;

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEmpleado = createString("idEmpleado");

    public final StringPath idFactura = createString("idFactura");

    public final StringPath idTipoD = createString("idTipoD");

    @NotNull
    public final QNotaVenta notaVenta;

    public final StringPath porcentaje = createString("porcentaje");

    public final StringPath tipoClave = createString("tipoClave");

    public QDescuento(String variable) {
        this(Descuento.class, forVariable(variable), INITS);
    }

    public QDescuento(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QDescuento(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(Descuento.class, metadata, inits);
    }

    public QDescuento(Class<? extends Descuento> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.descuentosClave = inits.isInitialized("descuentosClave") ? new QDescuentoClave(forProperty("descuentosClave")) : null;
        this.notaVenta = inits.isInitialized("notaVenta") ? new QNotaVenta(forProperty("notaVenta"), inits.get("notaVenta")) : null;
    }

}

