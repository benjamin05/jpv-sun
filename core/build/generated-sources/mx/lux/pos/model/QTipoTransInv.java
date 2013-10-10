package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QTipoTransInv is a Querydsl query type for TipoTransInv
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTipoTransInv extends EntityPathBase<TipoTransInv> {

    private static final long serialVersionUID = 866171646;

    public static final QTipoTransInv tipoTransInv = new QTipoTransInv("tipoTransInv");

    public final StringPath descripcion = createString("descripcion");

    public final StringPath idTipoTrans = createString("idTipoTrans");

    public final StringPath tipoMov = createString("tipoMov");

    public final NumberPath<Integer> ultimoFolio = createNumber("ultimoFolio", Integer.class);

    public QTipoTransInv(String variable) {
        super(TipoTransInv.class, forVariable(variable));
    }

    public QTipoTransInv(@NotNull Path<? extends TipoTransInv> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QTipoTransInv(PathMetadata<?> metadata) {
        super(TipoTransInv.class, metadata);
    }

}

