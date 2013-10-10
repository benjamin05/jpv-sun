package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QResumenDiario is a Querydsl query type for ResumenDiario
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QResumenDiario extends EntityPathBase<ResumenDiario> {

    private static final long serialVersionUID = 1490277628;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QResumenDiario resumenDiario = new QResumenDiario("resumenDiario");

    public final NumberPath<Integer> facturas = createNumber("facturas", Integer.class);

    public final DateTimePath<java.util.Date> fechaCierre = createDateTime("fechaCierre", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaMod = createDateTime("fechaMod", java.util.Date.class);

    @NotNull
    public final QFormaPago formaPago;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idTerminal = createString("idTerminal");

    public final NumberPath<java.math.BigDecimal> importe = createNumber("importe", java.math.BigDecimal.class);

    public final StringPath orden = createString("orden");

    public final StringPath plan = createString("plan");

    @NotNull
    public final QTerminal terminal;

    public final StringPath tipo = createString("tipo");

    public final NumberPath<Integer> vouchers = createNumber("vouchers", Integer.class);

    public QResumenDiario(String variable) {
        this(ResumenDiario.class, forVariable(variable), INITS);
    }

    public QResumenDiario(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QResumenDiario(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(ResumenDiario.class, metadata, inits);
    }

    public QResumenDiario(Class<? extends ResumenDiario> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.formaPago = inits.isInitialized("formaPago") ? new QFormaPago(forProperty("formaPago")) : null;
        this.terminal = inits.isInitialized("terminal") ? new QTerminal(forProperty("terminal"), inits.get("terminal")) : null;
    }

}

