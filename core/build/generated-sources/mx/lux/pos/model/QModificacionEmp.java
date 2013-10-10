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
 * QModificacionEmp is a Querydsl query type for ModificacionEmp
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QModificacionEmp extends EntityPathBase<ModificacionEmp> {

    private static final long serialVersionUID = 589378330;

    public static final QModificacionEmp modificacionEmp = new QModificacionEmp("modificacionEmp");

    public final StringPath empleadoActual = createString("empleadoActual");

    public final StringPath empleadoAnterior = createString("empleadoAnterior");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public QModificacionEmp(String variable) {
        super(ModificacionEmp.class, forVariable(variable));
    }

    public QModificacionEmp(@NotNull Path<? extends ModificacionEmp> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QModificacionEmp(PathMetadata<?> metadata) {
        super(ModificacionEmp.class, metadata);
    }

}

