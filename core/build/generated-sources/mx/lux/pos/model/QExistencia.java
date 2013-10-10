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
 * QExistencia is a Querydsl query type for Existencia
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QExistencia extends EntityPathBase<Existencia> {

    private static final long serialVersionUID = -1186613604;

    public static final QExistencia existencia = new QExistencia("existencia");

    public final NumberPath<Integer> cantidad = createNumber("cantidad", Integer.class);

    public final DateTimePath<java.util.Date> fechaMod = createDateTime("fechaMod", java.util.Date.class);

    public final NumberPath<Integer> idArticulo = createNumber("idArticulo", Integer.class);

    public final StringPath idMod = createString("idMod");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath idSync = createString("idSync");

    public QExistencia(String variable) {
        super(Existencia.class, forVariable(variable));
    }

    public QExistencia(@NotNull Path<? extends Existencia> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QExistencia(PathMetadata<?> metadata) {
        super(Existencia.class, metadata);
    }

}

