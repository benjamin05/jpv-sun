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
 * QLocalidad is a Querydsl query type for Localidad
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLocalidad extends EntityPathBase<Localidad> {

    private static final long serialVersionUID = -1664754552;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QLocalidad localidad = new QLocalidad("localidad");

    public final NumberPath<Integer> actualiza = createNumber("actualiza", Integer.class);

    public final StringPath asentamiento = createString("asentamiento");

    public final StringPath ciudad = createString("ciudad");

    public final StringPath clase = createString("clase");

    public final StringPath codigo = createString("codigo");

    public final StringPath cor = createString("cor");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEstado = createString("idEstado");

    public final StringPath idLocalidad = createString("idLocalidad");

    @NotNull
    public final QMunicipio municipio;

    public final StringPath oficina = createString("oficina");

    public final StringPath reparto = createString("reparto");

    public final StringPath servicios = createString("servicios");

    public final StringPath usuario = createString("usuario");

    public QLocalidad(String variable) {
        this(Localidad.class, forVariable(variable), INITS);
    }

    public QLocalidad(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QLocalidad(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(Localidad.class, metadata, inits);
    }

    public QLocalidad(Class<? extends Localidad> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.municipio = inits.isInitialized("municipio") ? new QMunicipio(forProperty("municipio"), inits.get("municipio")) : null;
    }

}

