package mx.lux.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.lux.pos.model.CierreDiario
import mx.lux.pos.model.Receta
//import sun.swing.StringUIClientPropertyKey
import org.apache.commons.lang.StringUtils

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
          clientName: receta.cliente.nombreCompleto,
          idClient: receta.idCliente,
          rxDate: receta.fechaReceta,
          useGlasses: receta.sUsoAnteojos,
          optometristName: receta.empleado.nombreCompleto,
          idOpt: receta.idOptometrista,
          typeOpt: receta.tipoOpt,
          odEsfR: receta.odEsfR,
          odCilR: receta.odCilR,
          odEjeR: receta.odEjeR,
          odAdcR: receta.odAdcR,
          odAdiR: receta.odAdiR,
          odPrismH: receta.odPrismaH,
          oiEsfR: receta.oiEsfR,
          oiCilR: receta.oiCilR,
          oiEjeR: receta.oiEjeR,
          oiAdcR: receta.oiAdcR,
          oiAdiR: receta.oiAdiR,
          oiPrismH: receta.oiPrismaH,
          diLejosR: receta.diLejosR,
          diCercaR: receta.diCercaR,
          odAvR: "20/${receta.odAvR}" ?: '',
          oiAvR: "20/${receta.oiAvR}" ?: '',
          altOblR: receta.altOblR,
          observacionesR: receta.observacionesR,
          fPrint: receta.fImpresa,
          idSync: receta.idSync,
          DateMod: receta.fechaMod,
          modId: receta.idMod,
          idStore: receta.idSucursal,
          diOd: receta.diOd,
          diOi: receta.diOi,
          materialArm: receta.material_arm,
          odPrismaV: receta.odPrismaV ?: '',
          oiPrismaV: receta.oiPrismaV ?: '',
          treatment: receta.tratamientos,
          udf5: receta.udf5,
          udf6: receta.udf6,
          idRxOri: receta.idRxOri,
          folio: receta.folio
      )
      return prescription
    }
    return null
  }

}
