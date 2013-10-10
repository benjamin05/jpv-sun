package mx.lux.pos.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TipoMov {
    ISSUE( "S", -1 ),
    RECEIPT( "E", 1 );

    private String codigo;
    private Integer factorES;

    TipoMov( @NotNull String pCodigo, Integer pFactor ) {
        codigo = pCodigo.trim().toUpperCase();
        factorES = pFactor;
    }

    public String getCodigo() {
        return codigo;
    }

    public Integer getFactor() {
        return factorES;
    }

    @Nullable
    public static TipoMov parse( String pString ) {
        TipoMov found = null;
        for ( TipoMov tipo : values() ) {
            if ( tipo.getCodigo().equalsIgnoreCase( pString ) || tipo.toString().equalsIgnoreCase( pString ) ) {
                found = tipo;
                break;
            }
        }
        return found;
    }
}
