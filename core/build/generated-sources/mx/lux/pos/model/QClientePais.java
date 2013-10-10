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
 * QClientePais is a Querydsl query type for ClientePais
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QClientePais extends EntityPathBase<ClientePais> {

    private static final long serialVersionUID = -7229356;

    public static final QClientePais clientePais = new QClientePais("clientePais");

    public final StringPath ciudad = createString("ciudad");

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath pais = createString("pais");

    public QClientePais(String variable) {
        super(ClientePais.class, forVariable(variable));
    }

    public QClientePais(@NotNull Path<? extends ClientePais> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QClientePais(PathMetadata<?> metadata) {
        super(ClientePais.class, metadata);
    }

}

