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
 * QBancoDeposito is a Querydsl query type for BancoDeposito
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QBancoDeposito extends EntityPathBase<BancoDeposito> {

    private static final long serialVersionUID = 1880570891;

    public static final QBancoDeposito bancoDeposito = new QBancoDeposito("bancoDeposito");

    public final StringPath cuenta = createString("cuenta");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath nombre = createString("nombre");

    public final StringPath tipo = createString("tipo");

    public QBancoDeposito(String variable) {
        super(BancoDeposito.class, forVariable(variable));
    }

    public QBancoDeposito(@NotNull Path<? extends BancoDeposito> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QBancoDeposito(PathMetadata<?> metadata) {
        super(BancoDeposito.class, metadata);
    }

}

