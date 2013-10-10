package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QTransInvDetalle is a Querydsl query type for TransInvDetalle
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTransInvDetalle extends EntityPathBase<TransInvDetalle> {

    private static final long serialVersionUID = 353276397;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QTransInvDetalle transInvDetalle = new QTransInvDetalle("transInvDetalle");

    public final NumberPath<Integer> cantidad = createNumber("cantidad", Integer.class);

    public final NumberPath<Integer> folio = createNumber("folio", Integer.class);

    public final StringPath idTipoTrans = createString("idTipoTrans");

    public final NumberPath<Integer> linea = createNumber("linea", Integer.class);

    public final NumberPath<Integer> numReg = createNumber("numReg", Integer.class);

    public final NumberPath<Integer> sku = createNumber("sku", Integer.class);

    public final StringPath tipoMov = createString("tipoMov");

    @NotNull
    public final QTransInv transInv;

    public QTransInvDetalle(String variable) {
        this(TransInvDetalle.class, forVariable(variable), INITS);
    }

    public QTransInvDetalle(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QTransInvDetalle(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(TransInvDetalle.class, metadata, inits);
    }

    public QTransInvDetalle(Class<? extends TransInvDetalle> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.transInv = inits.isInitialized("transInv") ? new QTransInv(forProperty("transInv")) : null;
    }

}

