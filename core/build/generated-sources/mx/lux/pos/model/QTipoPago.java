package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QTipoPago is a Querydsl query type for TipoPago
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTipoPago extends EntityPathBase<TipoPago> {

    private static final long serialVersionUID = -750287890;

    public static final QTipoPago tipoPago = new QTipoPago("tipoPago");

    public final StringPath descripcion = createString("descripcion");

    public final StringPath f1 = createString("f1");

    public final StringPath f2 = createString("f2");

    public final StringPath f3 = createString("f3");

    public final StringPath f4 = createString("f4");

    public final StringPath f5 = createString("f5");

    public final StringPath id = createString("id");

    public final StringPath tipoCon = createString("tipoCon");

    public final StringPath tipoSoi = createString("tipoSoi");

    public QTipoPago(String variable) {
        super(TipoPago.class, forVariable(variable));
    }

    public QTipoPago(@NotNull Path<? extends TipoPago> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QTipoPago(PathMetadata<?> metadata) {
        super(TipoPago.class, metadata);
    }

}

