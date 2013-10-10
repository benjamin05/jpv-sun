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
 * QModificacionPag is a Querydsl query type for ModificacionPag
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QModificacionPag extends EntityPathBase<ModificacionPag> {

    private static final long serialVersionUID = 589388520;

    public static final QModificacionPag modificacionPag = new QModificacionPag("modificacionPag");

    public final NumberPath<Integer> bancoNuevo = createNumber("bancoNuevo", Integer.class);

    public final NumberPath<Integer> bancoViejo = createNumber("bancoViejo", Integer.class);

    public final StringPath estadoViejo = createString("estadoViejo");

    public final StringPath formaPagoNuevo = createString("formaPagoNuevo");

    public final StringPath formaPagoViejo = createString("formaPagoViejo");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> idPagoNuevo = createNumber("idPagoNuevo", Integer.class);

    public final NumberPath<Integer> idPagoViejo = createNumber("idPagoViejo", Integer.class);

    public final NumberPath<java.math.BigDecimal> montoNuevo = createNumber("montoNuevo", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> montoViejo = createNumber("montoViejo", java.math.BigDecimal.class);

    public final StringPath referenciaNuevo = createString("referenciaNuevo");

    public final StringPath referenciaViejo = createString("referenciaViejo");

    public QModificacionPag(String variable) {
        super(ModificacionPag.class, forVariable(variable));
    }

    public QModificacionPag(@NotNull Path<? extends ModificacionPag> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QModificacionPag(PathMetadata<?> metadata) {
        super(ModificacionPag.class, metadata);
    }

}

