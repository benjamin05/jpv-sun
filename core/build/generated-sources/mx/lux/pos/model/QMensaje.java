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
 * QMensaje is a Querydsl query type for Mensaje
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QMensaje extends EntityPathBase<Mensaje> {

    private static final long serialVersionUID = 1959883454;

    public static final QMensaje mensaje = new QMensaje("mensaje");

    public final StringPath clave = createString("clave");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath texto = createString("texto");

    public QMensaje(String variable) {
        super(Mensaje.class, forVariable(variable));
    }

    public QMensaje(@NotNull Path<? extends Mensaje> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QMensaje(PathMetadata<?> metadata) {
        super(Mensaje.class, metadata);
    }

}

