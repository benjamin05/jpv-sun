package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QContribuyente is a Querydsl query type for Contribuyente
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QContribuyente extends EntityPathBase<Contribuyente> {

    private static final long serialVersionUID = 770098166;

    public static final QContribuyente contribuyente = new QContribuyente("contribuyente");

    public final StringPath ciudad = createString("ciudad");

    public final StringPath codigoPostal = createString("codigoPostal");

    public final StringPath colonia = createString("colonia");

    public final StringPath domicilio = createString("domicilio");

    public final StringPath email = createString("email");

    public final DateTimePath<java.util.Date> fechaModificacion = createDateTime("fechaModificacion", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaRegistro = createDateTime("fechaRegistro", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> idCliente = createNumber("idCliente", Integer.class);

    public final StringPath idEstado = createString("idEstado");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath idSync = createString("idSync");

    public final BooleanPath impresion = createBoolean("impresion");

    public final StringPath nombre = createString("nombre");

    public final StringPath rfc = createString("rfc");

    public final StringPath telefono = createString("telefono");

    public QContribuyente(String variable) {
        super(Contribuyente.class, forVariable(variable));
    }

    public QContribuyente(@NotNull Path<? extends Contribuyente> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QContribuyente(PathMetadata<?> metadata) {
        super(Contribuyente.class, metadata);
    }

}

