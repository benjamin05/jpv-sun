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
 * QApertura is a Querydsl query type for Apertura
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QApertura extends EntityPathBase<Apertura> {

    private static final long serialVersionUID = 2102554509;

    public static final QApertura apertura = new QApertura("apertura");

    public final NumberPath<java.math.BigDecimal> efvoDolares = createNumber("efvoDolares", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> efvoPesos = createNumber("efvoPesos", java.math.BigDecimal.class);

    public final DateTimePath<java.util.Date> fechaApertura = createDateTime("fechaApertura", java.util.Date.class);

    public final StringPath observaciones = createString("observaciones");

    public QApertura(String variable) {
        super(Apertura.class, forVariable(variable));
    }

    public QApertura(@NotNull Path<? extends Apertura> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QApertura(PathMetadata<?> metadata) {
        super(Apertura.class, metadata);
    }

}

