package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QPlan is a Querydsl query type for Plan
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPlan extends EntityPathBase<Plan> {

    private static final long serialVersionUID = 2131281834;

    public static final QPlan plan = new QPlan("plan");

    public final StringPath descripcion = createString("descripcion");

    public final StringPath id = createString("id");

    public QPlan(String variable) {
        super(Plan.class, forVariable(variable));
    }

    public QPlan(@NotNull Path<? extends Plan> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QPlan(PathMetadata<?> metadata) {
        super(Plan.class, metadata);
    }

}

