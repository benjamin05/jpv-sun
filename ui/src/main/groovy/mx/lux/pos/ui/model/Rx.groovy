package mx.lux.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.model.Receta
import org.apache.commons.lang.StringUtils

//import sun.swing.StringUIClientPropertyKey
@Bindable
@ToString
@EqualsAndHashCode
class Rx {
  Integer id
  Integer exam
  String clientName
  Integer idClient
  String folio
  Date rxDate
  String useGlasses
  String optometristName
  String idOpt
  String typeOpt
  String odEsfR
  String odCilR
  String odEjeR
  String odAdcR
  String odAdiR
  String odPrismH
  String oiEsfR
  String oiCilR
  String oiEjeR
  String oiAdcR
  String oiAdiR
  String oiPrismH
  String diLejosR
  String diCercaR
  String odAvR
  String oiAvR
  String altOblR
  String observacionesR
  boolean fPrint
  String idSync
  Date DateMod
  String modId
  Integer idStore
  String diOd
  String diOi
  String materialArm
  String odPrismaV
  String oiPrismaV
  String treatment
  String udf5
  String udf6
  String idRxOri
  Order order


    String getTipo(){
        String tipo = ''
        if( useGlasses.equalsIgnoreCase('l') ){
            tipo = 'LEJOS'
        } else if( useGlasses.equalsIgnoreCase('c') ){
            tipo = 'CERCA'
        } else if( useGlasses.equalsIgnoreCase('b') ){
            tipo = 'BIFOCAL'
        } else if( useGlasses.equalsIgnoreCase('p') ){
            tipo = 'PROGRESIVO'
        } else if( useGlasses.equalsIgnoreCase('i') ){
            tipo = 'INTERMEDIO'
        } else if( useGlasses.equalsIgnoreCase('t') ){
            tipo = 'BIFOCAL INTERMEDIO'
        }

        return tipo
    }

    String getOptNameFormatter(){
      String name = ''
        if(StringUtils.trimToEmpty(optometristName) != '' && StringUtils.trimToEmpty(idOpt) != ''){
          name = '['+idOpt.trim()+']'+optometristName
        }
    }

    String getTipoEditRx(){
        String tipo = ''
        if( useGlasses.equalsIgnoreCase('l') ){
            tipo = 'MONOFOCAL'
        } else if( useGlasses.equalsIgnoreCase('c') ){
            tipo = 'MONOFOCAL'
        } else if( useGlasses.equalsIgnoreCase('b') ){
            tipo = 'BIFOCAL'
        } else if( useGlasses.equalsIgnoreCase('p') ){
            tipo = 'PROGRESIVO'
        }

        return tipo
    }

    String getTipoCorto( String tipo ){
        String tipoCorto = ''
        if(StringUtils.trimToEmpty( tipo ) ){
            if( tipo.trim().equalsIgnoreCase( 'LEJOS' ) ){
                tipoCorto = 'l'
            } else if( tipo.trim().equalsIgnoreCase( 'CERCA' ) ){
                tipoCorto = 'c'
            } else if( tipo.trim().equalsIgnoreCase( 'BIFOCAL' ) ){
                tipoCorto = 'b'
            } else if( tipo.trim().equalsIgnoreCase( 'PROGRESIVO' ) ){
                tipoCorto = 'p'
            } else if( tipo.trim().equalsIgnoreCase( 'INTERMEDIO' ) ){
                tipoCorto = 'i'
            } else if( tipo.trim().equalsIgnoreCase( 'BIFOCAL INTERMEDIO' ) ){
                tipoCorto = 't'
            }
        }
    }

  static Rx toRx( Receta receta ) {
    if ( receta?.id ) {
      Rx prescription = new Rx(
          id: receta.id,
          exam: receta.examen,
          clientName: receta?.cliente?.nombreCompleto,
          idClient: receta.idCliente,
          rxDate: receta.fechaReceta,
          useGlasses: receta.sUsoAnteojos,
          optometristName: receta?.empleado?.nombre.trim()+' '+receta?.empleado?.apellidoPaterno.trim()+' '+receta?.empleado?.apellidoMaterno.trim(),
          idOpt: receta.idOptometrista,
          typeOpt: StringUtils.trimToEmpty(receta.tipoOpt),
          odEsfR: StringUtils.trimToEmpty(receta.odEsfR),
          odCilR: StringUtils.trimToEmpty(receta.odCilR),
          odEjeR: StringUtils.trimToEmpty(receta.odEjeR),
          odAdcR: StringUtils.trimToEmpty(receta.odAdcR),
          odAdiR: StringUtils.trimToEmpty(receta.odAdiR),
          odPrismH: StringUtils.trimToEmpty(receta.odPrismaH),
          oiEsfR: StringUtils.trimToEmpty(receta.oiEsfR),
          oiCilR: StringUtils.trimToEmpty(receta.oiCilR),
          oiEjeR: StringUtils.trimToEmpty(receta.oiEjeR),
          oiAdcR: StringUtils.trimToEmpty(receta.oiAdcR),
          oiAdiR: StringUtils.trimToEmpty(receta.oiAdiR),
          oiPrismH: StringUtils.trimToEmpty(receta.oiPrismaH),
          diLejosR: StringUtils.trimToEmpty(receta.diLejosR),
          diCercaR: StringUtils.trimToEmpty(receta.diCercaR),
          odAvR: "20/${StringUtils.trimToEmpty(receta.odAvR)}" ?: '',
          oiAvR: "20/${StringUtils.trimToEmpty(receta.oiAvR)}" ?: '',
          altOblR: StringUtils.trimToEmpty(receta.altOblR),
          observacionesR: StringUtils.trimToEmpty(receta.observacionesR),
          fPrint: receta.fImpresa,
          idSync: receta.idSync,
          DateMod: receta.fechaMod,
          modId: receta.idMod,
          idStore: receta.idSucursal,
          diOd: StringUtils.trimToEmpty(receta.diOd),
          diOi: StringUtils.trimToEmpty(receta.diOi),
          materialArm: StringUtils.trimToEmpty(receta.material_arm),
          odPrismaV: receta.odPrismaV ?: '',
          oiPrismaV: receta.oiPrismaV ?: '',
          treatment: receta.tratamientos,
          udf5: receta.udf5,
          udf6: receta.udf6,
          idRxOri: receta.idRxOri,
          folio: receta.folio,
          order: Order.toOrder(receta?.notaVenta != null ? receta?.notaVenta : new NotaVenta())
      )
      return prescription
    }
    return null
  }

}
