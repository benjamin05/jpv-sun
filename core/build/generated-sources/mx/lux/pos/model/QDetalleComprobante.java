package mx.lux.pos.model;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QDetalleComprobante is a Querydsl query type for DetalleComprobante
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDetalleComprobante extends EntityPathBase<DetalleComprobante> {

    private static final long serialVersionUID = 1230198142;

    public static final QDetalleComprobante detalleComprobante = new QDetalleComprobante("detalleComprobante");

    public final StringPath articulo = createString("articulo");

    public final StringPath cantidad = createString("cantidad");

    public final StringPath color = createString("color");

    public final StringPath descripcion = createString("descripcion");

    public final DateTimePath<java.util.Date> fechaModificacion = createDateTime("fechaModificacion", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idArticulo = createString("idArticulo");

    public final StringPath idFiscal = createString("idFiscal");

    public final StringPath idGenerico = createString("idGenerico");

    public final NumberPath<java.math.BigDecimal> importe = createNumber("importe", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> precioUnitario = createNumber("precioUnitario", java.math.BigDecimal.class);

    public QDetalleComprobante(String variable) {
        super(DetalleComprobante.class, forVariable(variable));
    }

    public QDetalleComprobante(@NotNull Path<? extends DetalleComprobante> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QDetalleComprobante(PathMetadata<?> metadata) {
        super(DetalleComprobante.class, metadata);
    }

}

