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
 * QCausaCancelacion is a Querydsl query type for CausaCancelacion
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QCausaCancelacion extends EntityPathBase<CausaCancelacion> {

    private static final long serialVersionUID = 1564885960;

    public static final QCausaCancelacion causaCancelacion = new QCausaCancelacion("causaCancelacion");

    public final StringPath descripcion = createString("descripcion");

    public final DateTimePath<java.util.Date> fechaModificacion = createDateTime("fechaModificacion", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idModificacion = createString("idModificacion");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath idSync = createString("idSync");

    public QCausaCancelacion(String variable) {
        super(CausaCancelacion.class, forVariable(variable));
    }

    public QCausaCancelacion(@NotNull Path<? extends CausaCancelacion> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QCausaCancelacion(PathMetadata<?> metadata) {
        super(CausaCancelacion.class, metadata);
    }

}

