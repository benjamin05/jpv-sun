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
 * QReimpresion is a Querydsl query type for Reimpresion
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QReimpresion extends EntityPathBase<Reimpresion> {

    private static final long serialVersionUID = -1334688320;

    public static final QReimpresion reimpresion = new QReimpresion("reimpresion");

    public final StringPath factura = createString("factura");

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEmpleado = createString("idEmpleado");

    public final StringPath idNota = createString("idNota");

    public final StringPath nota = createString("nota");

    public QReimpresion(String variable) {
        super(Reimpresion.class, forVariable(variable));
    }

    public QReimpresion(@NotNull Path<? extends Reimpresion> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QReimpresion(PathMetadata<?> metadata) {
        super(Reimpresion.class, metadata);
    }

}

