package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QEntregadoExterno is a Querydsl query type for EntregadoExterno
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEntregadoExterno extends EntityPathBase<EntregadoExterno> {

    private static final long serialVersionUID = 2076088845;

    public static final QEntregadoExterno entregadoExterno = new QEntregadoExterno("entregadoExterno");

    public final StringPath facturaTxt = createString("facturaTxt");

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaPago = createDateTime("fechaPago", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idFactura = createString("idFactura");

    public final StringPath idSucursal = createString("idSucursal");

    public final NumberPath<java.math.BigDecimal> pago = createNumber("pago", java.math.BigDecimal.class);

    public QEntregadoExterno(String variable) {
        super(EntregadoExterno.class, forVariable(variable));
    }

    public QEntregadoExterno(@NotNull Path<? extends EntregadoExterno> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QEntregadoExterno(PathMetadata<?> metadata) {
        super(EntregadoExterno.class, metadata);
    }

}

