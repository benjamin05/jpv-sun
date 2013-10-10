package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QMunicipio is a Querydsl query type for Municipio
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QMunicipio extends EntityPathBase<Municipio> {

    private static final long serialVersionUID = -211942388;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QMunicipio municipio = new QMunicipio("municipio");

    @NotNull
    public final QEstado estado;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEstado = createString("idEstado");

    public final StringPath idLocalidad = createString("idLocalidad");

    public final StringPath nombre = createString("nombre");

    public final StringPath rango1 = createString("rango1");

    public final StringPath rango2 = createString("rango2");

    public final StringPath rango3 = createString("rango3");

    public final StringPath rango4 = createString("rango4");

    public QMunicipio(String variable) {
        this(Municipio.class, forVariable(variable), INITS);
    }

    public QMunicipio(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QMunicipio(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(Municipio.class, metadata, inits);
    }

    public QMunicipio(Class<? extends Municipio> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.estado = inits.isInitialized("estado") ? new QEstado(forProperty("estado")) : null;
    }

}

