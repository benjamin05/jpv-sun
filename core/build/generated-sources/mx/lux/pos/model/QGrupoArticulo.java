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
 * QGrupoArticulo is a Querydsl query type for GrupoArticulo
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QGrupoArticulo extends EntityPathBase<GrupoArticulo> {

    private static final long serialVersionUID = -1812778749;

    public static final QGrupoArticulo grupoArticulo = new QGrupoArticulo("grupoArticulo");

    public final StringPath descripcion = createString("descripcion");

    public final NumberPath<Integer> idGrupo = createNumber("idGrupo", Integer.class);

    public QGrupoArticulo(String variable) {
        super(GrupoArticulo.class, forVariable(variable));
    }

    public QGrupoArticulo(@NotNull Path<? extends GrupoArticulo> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QGrupoArticulo(PathMetadata<?> metadata) {
        super(GrupoArticulo.class, metadata);
    }

}

