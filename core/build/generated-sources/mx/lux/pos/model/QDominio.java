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
 * QDominio is a Querydsl query type for Dominio
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDominio extends EntityPathBase<Dominio> {

    private static final long serialVersionUID = -1447599828;

    public static final QDominio dominio = new QDominio("dominio");

    public final DateTimePath<java.util.Date> fechaModificado = createDateTime("fechaModificado", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath nombre = createString("nombre");

    public QDominio(String variable) {
        super(Dominio.class, forVariable(variable));
    }

    public QDominio(@NotNull Path<? extends Dominio> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QDominio(PathMetadata<?> metadata) {
        super(Dominio.class, metadata);
    }

}

