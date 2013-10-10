package mx.lux.pos.model;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QPago is a Querydsl query type for Pago
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPago extends EntityPathBase<Pago> {

    private static final long serialVersionUID = 2131271450;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QPago pago = new QPago("pago");

    public final StringPath clave = createString("clave");

    public final BooleanPath confirmado = createBoolean("confirmado");

    @NotNull
    public final QEmpleado empleado;

    @NotNull
    public final QTipoPago eTipoPago;

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaModificacion = createDateTime("fechaModificacion", java.util.Date.class);

    @NotNull
    public final QFormaPago formaPago;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idBanco = createString("idBanco");

    public final StringPath idBancoEmisor = createString("idBancoEmisor");

    public final StringPath idEmpleado = createString("idEmpleado");

    public final StringPath idFactura = createString("idFactura");

    public final StringPath idFormaPago = createString("idFormaPago");

    public final StringPath idFPago = createString("idFPago");

    public final StringPath idMod = createString("idMod");

    public final StringPath idPlan = createString("idPlan");

    public final StringPath idRecibo = createString("idRecibo");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath idSync = createString("idSync");

    public final StringPath idTerminal = createString("idTerminal");

    public final NumberPath<java.math.BigDecimal> monto = createNumber("monto", java.math.BigDecimal.class);

    @NotNull
    public final QNotaVenta notaVenta;

    public final StringPath parcialidad = createString("parcialidad");

    @NotNull
    public final QPlan plan;

    public final NumberPath<java.math.BigDecimal> porDevolver = createNumber("porDevolver", java.math.BigDecimal.class);

    public final StringPath referenciaClave = createString("referenciaClave");

    public final StringPath referenciaPago = createString("referenciaPago");

    @NotNull
    public final QSucursal sucursal;

    @NotNull
    public final QTerminal terminal;

    public final StringPath tipoPago = createString("tipoPago");

    public QPago(String variable) {
        this(Pago.class, forVariable(variable), INITS);
    }

    public QPago(@NotNull PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QPago(PathMetadata<?> metadata, @NotNull PathInits inits) {
        this(Pago.class, metadata, inits);
    }

    public QPago(Class<? extends Pago> type, PathMetadata<?> metadata, @NotNull PathInits inits) {
        super(type, metadata, inits);
        this.empleado = inits.isInitialized("empleado") ? new QEmpleado(forProperty("empleado"), inits.get("empleado")) : null;
        this.eTipoPago = inits.isInitialized("eTipoPago") ? new QTipoPago(forProperty("eTipoPago")) : null;
        this.formaPago = inits.isInitialized("formaPago") ? new QFormaPago(forProperty("formaPago")) : null;
        this.notaVenta = inits.isInitialized("notaVenta") ? new QNotaVenta(forProperty("notaVenta"), inits.get("notaVenta")) : null;
        this.plan = inits.isInitialized("plan") ? new QPlan(forProperty("plan")) : null;
        this.sucursal = inits.isInitialized("sucursal") ? new QSucursal(forProperty("sucursal"), inits.get("sucursal")) : null;
        this.terminal = inits.isInitialized("terminal") ? new QTerminal(forProperty("terminal"), inits.get("terminal")) : null;
    }

}

