package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QTrabajoEstado is a Querydsl query type for TrabajoEstado
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTrabajoEstado extends EntityPathBase<TrabajoEstado> {

    private static final long serialVersionUID = 1270374092;

    public static final QTrabajoEstado trabajoEstado = new QTrabajoEstado("trabajoEstado");

    public final StringPath descr = createString("descr");

    public final StringPath id = createString("id");

    public final StringPath llamada = createString("llamada");

    public QTrabajoEstado(String variable) {
        super(TrabajoEstado.class, forVariable(variable));
    }

    public QTrabajoEstado(@NotNull Path<? extends TrabajoEstado> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QTrabajoEstado(PathMetadata<?> metadata) {
        super(TrabajoEstado.class, metadata);
    }

}

