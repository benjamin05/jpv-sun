package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QEstado is a Querydsl query type for Estado
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEstado extends EntityPathBase<Estado> {

    private static final long serialVersionUID = -845456985;

    public static final QEstado estado = new QEstado("estado");

    public final StringPath edo1 = createString("edo1");

    public final StringPath id = createString("id");

    public final StringPath nombre = createString("nombre");

    public final StringPath rango1 = createString("rango1");

    public final StringPath rango2 = createString("rango2");

    public QEstado(String variable) {
        super(Estado.class, forVariable(variable));
    }

    public QEstado(@NotNull Path<? extends Estado> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QEstado(PathMetadata<?> metadata) {
        super(Estado.class, metadata);
    }

}

