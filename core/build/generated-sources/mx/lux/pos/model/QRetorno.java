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
 * QRetorno is a Querydsl query type for Retorno
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRetorno extends EntityPathBase<Retorno> {

    private static final long serialVersionUID = 2107872996;

    public static final QRetorno retorno = new QRetorno("retorno");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<java.math.BigDecimal> monto = createNumber("monto", java.math.BigDecimal.class);

    public final StringPath ticketDestino = createString("ticketDestino");

    public final StringPath ticketOrigen = createString("ticketOrigen");

    public QRetorno(String variable) {
        super(Retorno.class, forVariable(variable));
    }

    public QRetorno(@NotNull Path<? extends Retorno> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QRetorno(PathMetadata<?> metadata) {
        super(Retorno.class, metadata);
    }

}

