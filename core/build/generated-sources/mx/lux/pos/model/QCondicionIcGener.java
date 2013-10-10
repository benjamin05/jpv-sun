package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QCondicionIcGener is a Querydsl query type for CondicionIcGener
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QCondicionIcGener extends EntityPathBase<CondicionIcGener> {

    private static final long serialVersionUID = -839838312;

    public static final QCondicionIcGener condicionIcGener = new QCondicionIcGener("condicionIcGener");

    public final BooleanPath enConvenio = createBoolean("enConvenio");

    public final DateTimePath<java.util.Date> fechaMod = createDateTime("fechaMod", java.util.Date.class);

    public final StringPath id = createString("id");

    public final StringPath idGenerico = createString("idGenerico");

    public final StringPath idMod = createString("idMod");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath idSync = createString("idSync");

    public final BooleanPath pagoConVales = createBoolean("pagoConVales");

    public final NumberPath<java.math.BigDecimal> porcentajeCopago = createNumber("porcentajeCopago", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> porcentajeDescto = createNumber("porcentajeDescto", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> topePago = createNumber("topePago", java.math.BigDecimal.class);

    public QCondicionIcGener(String variable) {
        super(CondicionIcGener.class, forVariable(variable));
    }

    public QCondicionIcGener(@NotNull Path<? extends CondicionIcGener> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QCondicionIcGener(PathMetadata<?> metadata) {
        super(CondicionIcGener.class, metadata);
    }

}

