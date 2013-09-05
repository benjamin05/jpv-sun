package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.Rx
import mx.lux.pos.model.Receta
import net.miginfocom.swing.MigLayout

import javax.swing.JTextArea
import java.awt.Component
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel
import javax.swing.JScrollPane
import org.apache.commons.lang3.StringUtils
import mx.lux.pos.ui.controller.CustomerController

import java.awt.Dimension
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import javax.swing.JComboBox
import javax.swing.JOptionPane
import mx.lux.pos.ui.model.UpperCaseDocument

class EditRxDialog extends JDialog {

    private def sb

    private Component component


    private Rx receta
    private Receta rec
    private Integer idCliente
    private Integer idSucursal

    private JTextField txtEmpleado
    private JTextField txtFolio
    private JLabel lblEmpleado
    private JLabel lblFolio
    private JComboBox cbUso

    private JTextField txtOdEsfera
    private JTextField txtOdCil
    private JTextField txtOdEje
    private JTextField txtOdAd
    private JTextField txtOdAv
    private JTextField txtOdDm
    private JTextField txtOdPrisma
    private JComboBox cbOdUbic

    private JTextField txtOiEsfera
    private JTextField txtOiCil
    private JTextField txtOiEje
    private JTextField txtOiAd
    private JTextField txtOiAv
    private JTextField txtOiDm
    private JTextField txtOiPrisma
    private JComboBox cbOiUbic

    private JTextField txtDICerca
    private JTextField txtAltOblea
    private JTextField txtDILejos

    private JTextArea txtObservaciones




    private JPanel empleadoPanel
    private final double VALOR_MULTIPLO = 0.25;
    private boolean mostrarParametroSV = true
    private boolean mostrarParametroP = true
    private boolean mostrarParametroB = true

    private static String itemUso = null
    private static String limpiarAux

    List<String> ubicacion = ["", "ARRIBA", "ABAJO", "AFUERA", "ADENTRO"]
    List<String> usoM = ["LEJOS", "CERCA"]
    List<String> usoP = ["PROGRESIVO"]
    List<String> usoB = ["BIFOCAL"]
    List<String> comboUso = []

    Boolean canceled
    String Title

    EditRxDialog(Component parent, Rx receta, Integer idCliente, Integer idSucursal, String titulo,String uso) {

        sb = new SwingBuilder()
        component = parent
        itemUso =  uso
        rec = null

        title = titulo
        if(itemUso.trim().equals('MONOFOCAL'))
        {
            mostrarParametroSV = true
            mostrarParametroP = false
            mostrarParametroB = false
            comboUso= usoM
        }else if(itemUso.trim().equals('BIFOCAL')){
            mostrarParametroSV = false
            mostrarParametroP = true
            mostrarParametroB = false
            comboUso= usoB
        }else if(itemUso.trim().equals('PROGRESIVO')){
            mostrarParametroSV = false
            mostrarParametroP = true
            mostrarParametroB = true
            comboUso= usoP
        }

        if (receta?.id == null) {
            this.receta = new Rx()
            this.idCliente = idCliente
            this.idSucursal = idSucursal
        } else {

            this.receta = receta

            this.idCliente = idCliente
            this.idSucursal = idSucursal
        }
        buildUI()
        doBindings()
    }

    // UI Layout Definition
    void buildUI() {
          sb.dialog(this,
                title: title,
                resizable: false,
                pack: true,
                modal: true,
                preferredSize: [520, 380],
                layout: new MigLayout('wrap,center', '[fill,grow]'),
                location: [ 200, 250 ],
        ) {
            empleadoPanel = panel(layout: new MigLayout('fill,wrap 3, left', '[fill][fill][fill,grow,left]')) {
                label(text: 'Optometrista:')
                txtEmpleado = textField( minimumSize: [50, 20], actionPerformed: {doOptSearch()})
                txtEmpleado.addFocusListener(new FocusListener() {
                    @Override
                    void focusGained(FocusEvent e) { }
                    @Override
                    void focusLost(FocusEvent e) {
                        if (txtEmpleado.text.length() > 0) {
                            doOptSearch()
                        }
                    }
                })
                lblEmpleado = label(border: titledBorder(title: ''), minimumSize: [150, 20])
                lblFolio = label(text: 'FolioPlantilla: ')
                txtFolio = textField( minimumSize: [50, 20])
                label()
                label(text: 'Uso:')
                cbUso = comboBox(items: comboUso)
                label()
            }

           /* panel( layout: new MigLayout( 'fill,wrap 3','[fill][fill][fill,grow]' )){
                label(text: 'Uso')
                cbUso = comboBox(items: comboUso)
                label()
            } */

            panel(border: titledBorder("Rx"), layout: new MigLayout('fill,wrap ,center', '[fill,grow]')) {
                panel(layout: new MigLayout('fill,wrap 8,center',
                        '''[center][fill,grow,center][fill,grow,center][fill,grow,center][fill,grow,center]
                            [fill,grow,center][center][fill,grow,center]''')) {
                    label()

                    label(text: 'Esfera', horizontalAlignment: JTextField.CENTER)
                    label(text: 'Cil.', toolTipText: 'Cilindro', horizontalAlignment: JTextField.CENTER )
                    label(text: 'Eje', horizontalAlignment: JTextField.CENTER )
                    label(text: 'Ad.', toolTipText: 'Adición', horizontalAlignment: JTextField.CENTER,visible:mostrarParametroP,enabled:  mostrarParametroP)


                    /*label(text: 'A.V.', toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.CENTER,visible:false )        */
                    label(text: 'D.M.', toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.CENTER,visible:(mostrarParametroP && mostrarParametroB),enabled:(mostrarParametroP && mostrarParametroB)  )
                    /*label(text: 'Prisma', horizontalAlignment: JTextField.CENTER,visible:false )   */
                    /*label(text: 'Ubic.', toolTipText: 'Ubicación', horizontalAlignment: JTextField.CENTER,visible:false )    */

                    label()
                    label()
                    label(text: 'O.D.', toolTipText: 'Ojo Derecho')
                    txtOdEsfera = textField( horizontalAlignment: JTextField.RIGHT )
                    txtOdEsfera.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdEsfera)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdEsfera,35,-35,0.25,'.00','+')

                        }
                    })

                    txtOdCil = textField(toolTipText: 'Cilindro', horizontalAlignment: JTextField.RIGHT )
                    txtOdCil.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                         limpiar(txtOdCil)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdCil,12,-12,0.25,'.00','-')
                        }
                    })

                    txtOdEje = textField(toolTipText: 'Eje',horizontalAlignment: JTextField.RIGHT )
                    txtOdEje.addFocusListener(new FocusListener() {
                            @Override
                            void focusGained(FocusEvent e) {
                                limpiar(txtOdEje)
                            }
                            @Override
                            void focusLost(FocusEvent e) {
                                validacion(txtOdEje,180,0,1,'0','')
                            }
                    })

                    txtOdAd = textField( toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroP,enabled:mostrarParametroP)
                    txtOdAd.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                             limpiar(txtOdAd)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdAd,4,0.75,0.25,'.00','+')
                    }
                    })
                    /*txtOdAv = textField( toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT,visible:false )
                    txtOdAv.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            txtOdAv.setSelectionStart(3)
                            txtOdAv.setSelectionEnd(5)
                        }

                        @Override
                        void focusLost(FocusEvent e) { }
                    })*/
                    txtOdDm = textField( toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.RIGHT,visible:(mostrarParametroP && mostrarParametroB),enabled:(mostrarParametroP && mostrarParametroB))
                    txtOdDm.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdDm)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdDm,45,22,0.1,'.0','')
                        }
                    })
                    /*txtOdPrisma = textField( horizontalAlignment: JTextField.RIGHT,visible:false)
                    txtOdPrisma.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdPrisma)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdPrisma,12,0,0.25,'.00','')
                        }
                    })*/
                    /*cbOdUbic = comboBox(items: ubicacion, toolTipText: 'Ubicación',visible:false)*/

                    /*label(text: 'D.I. Cerca', toolTipText: 'Distancia Interpupilar Cerca',visible:false )
                    txtDICerca = textField( minimumSize: [20, 20], toolTipText: 'Distancia Interpupilar Cerca', horizontalAlignment: JTextField.RIGHT,visible:false )
                    txtDICerca.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtDICerca)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtDICerca,90,45,1,'0','')
                        }
                    })*/
                    label(text: 'D.I. Binocular', toolTipText: 'Distancia Interpupilar Binocular',visible:(mostrarParametroP && !mostrarParametroB)||mostrarParametroSV ,enabled:(mostrarParametroP && !mostrarParametroB)||mostrarParametroSV)
                    txtDILejos = textField(minimumSize: [20, 20], toolTipText: 'Distancia Interpupilar Binocular', horizontalAlignment: JTextField.RIGHT,visible:(mostrarParametroP && !mostrarParametroB)||mostrarParametroSV  ,enabled:(mostrarParametroP && !mostrarParametroB)||mostrarParametroSV )
                    txtDILejos.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtDILejos)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtDILejos,90,45,1,'0','')
                        }
                    })

                    label(text: 'O.I.', toolTipText: 'Ojo Izquierdo')
                    txtOiEsfera = textField( horizontalAlignment: JTextField.RIGHT )
                    txtOiEsfera.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiEsfera)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiEsfera,35,-35,0.25,'.00','+')

                        }
                    })
                    txtOiCil = textField( toolTipText: 'Cilindro', horizontalAlignment: JTextField.RIGHT )
                    txtOiCil.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiCil)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiCil,12,-12,0.25,'.00','-')
                        }
                    })

                    txtOiEje = textField( horizontalAlignment: JTextField.RIGHT )
                    txtOiEje.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiEje)
                        }
                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiEje,180,0,1,'0','')
                        }
                    })

                    txtOiAd = textField( toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroP,enabled:mostrarParametroP )
                    txtOiAd.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiAd)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiAd,4,0.75,0.25,'.00','+')
                        }
                    })
                    /*txtOiAv = textField( toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT,visible:false)
                    txtOiAv.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            txtOiAv.setSelectionStart(3)
                            txtOiAv.setSelectionEnd(5)
                        }

                        @Override
                        void focusLost(FocusEvent e) { }
                    }) */
                    txtOiDm = textField( toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.RIGHT,visible:(mostrarParametroP && mostrarParametroB),enabled:(mostrarParametroP && mostrarParametroB) )
                        txtOiDm.addFocusListener(new FocusListener() {
                            @Override
                            void focusGained(FocusEvent e) {
                                limpiar(txtOiDm)
                            }

                            @Override
                            void focusLost(FocusEvent e) {
                                validacion(txtOiDm,45,22,0.1,'.0','')
                            }
                        })
                    /*txtOiPrisma = textField( horizontalAlignment: JTextField.RIGHT,visible:false)
                    txtOiPrisma.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiPrisma)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiPrisma,12,0,0.25,'.00','')
                        }
                    })*/
                    /*cbOiUbic = comboBox(items: ubicacion, toolTipText: 'Ubicación',visible:false)*/
                    label(text: 'Alt. Seg.', toolTipText: 'Altura Segmento',visible:mostrarParametroP,enabled:mostrarParametroP)
                    txtAltOblea = textField( minimumSize: [20, 20], toolTipText: 'Altura Segmento', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroP,enabled:mostrarParametroP )
                    txtAltOblea.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtAltOblea)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtAltOblea,40,10,0.5,'.00','')
                        }
                    })

                }
                scrollPane( border: titledBorder( title: 'Observaciones' ) ) {
                    txtObservaciones = textArea(document: new UpperCaseDocument(), lineWrap: true )

                }
            }
            panel(layout: new MigLayout('wrap 2,right', '[right][right]')) {
                button(text: 'Guardar', actionPerformed: {doRxSave()}, maximumSize: [110, 90])
                button(text: 'Cancelar', actionPerformed: {doCancel()}, maximumSize: [110, 90])
            }
        }
    }

    private void limpiar(JTextField txtField){
        //limpiarAux = txtField.text
        //txtField.text = txtField.text.substring(1,txtField.text.indexOf('.'))
      /*
        if(txtField.text != ''){
            if(txtField.text.substring(txtField.text.indexOf('.') + 1,txtField.text.size()).toInteger() > 0){
                 txtField.text = txtField.text.substring(1,txtField.text.indexOf('.')) + '.' + txtField.text.substring(txtField.text.indexOf('.') + 1,txtField.text.size())
            }   else{
                txtField.text = txtField.text.substring(1,txtField.text.indexOf('.'))
            }
        }
           */
        //txtField.text = ''

    }

    private void validacion(JTextField txtField, double max, double min, double interval, String format, String mask){
        if (txtField.text.trim().length() > 0 || txtField.text.trim() == '0') {
            double number
            String txt = txtField.text.trim()
            String signo = ''
            if(txt.substring(0,1)=='-'){
                  txt = txt.substring(1,txtField.text.trim().size())
                  signo = '-'
            } else if(txt.substring(0,1)=='+'){
                txt = txt.substring(1,txtField.text.trim().size())
            }
            if(txt.substring(0,1)=='0') {
                txt = txt.substring(1,txtField.text.trim().size())
            }
            println('Valor: ' + txt)
            println('Signo: ' + signo)
            txtField.text = ''
            Double multiplo = 0.0
            if (txt.length() > 0) {
                number = Double.parseDouble(txt);
                multiplo = number / interval;
                multiplo = multiplo % 1

                if (multiplo ==0 || multiplo.toString().equals('-0.0')) {
                    if (number >= min && number <= max){
                        if (format.equals('.00') &&  number.toString().substring(number.toString().indexOf('.'),number.toString().length()).equals('.0'))
                        {
                            txt=txt + '.00'
                        }
                        else if(format.equals('.0')&& number.toString().substring(number.toString().indexOf('.'),number.toString().length()).equals('.0'))
                        {
                            txt=txt + '.0'
                        }
                        println('val: '+txt)
                        if(number>-1 && number<1 && number != 0){
                           txt = '0' + txt
                        }
                        if(mask.equals('+')){
                          txtField.text = signoMas(signo + txt)
                        }else if(mask.equals('-')){
                            txtField.text = signoMenos(signo + txt)
                        }else{
                            txtField.text = txt
                        }
                    }else{
                        txtField.text = ''
                    }
                } else {
                    txtField.text = ''
                }
            }else{
                txtField.text = ''
            }
        } else {
            txtField.text = limpiarAux
        }
    }

    private void doBindings() {
        sb.build {
            txtOdEsfera.setText(receta.odEsfR)
            txtOdCil.setText(receta.odCilR)
            txtOdEje.setText(receta.odEjeR)
            txtOdAd.setText(receta.odAdcR)
       /*     if (receta.odAvR != null) {
                txtOdAv.setText(receta.odAvR)
            } else {
                txtOdAv.setText("20/")
            }
            */
            txtOdDm.setText(receta.diOd)
           // txtOdPrisma.setText(receta.odPrismH)
            txtDILejos.setText(receta.diLejosR)
            txtOiEsfera.setText(receta.oiEsfR)
            txtOiCil.setText(receta.oiCilR)
            txtOiEje.setText(receta.oiEjeR)
           /* if (receta.oiAvR != null) {
                txtOiAv.setText(receta.oiAvR)
            } else {
                txtOiAv.setText("20/")
            }
            */
            txtOiDm.setText(receta.diOi)
            txtOiAd.setText(receta.oiAdcR)
            //txtOiPrisma.setText(receta.oiPrismH)
            /*if (receta.odPrismaV != null) {
                cbOdUbic.setSelectedItem(receta.odPrismaV)
            } else {
                cbOdUbic.setSelectedItem('')
            }
            if (receta.oiPrismaV != null) {
                cbOiUbic.setSelectedItem(receta.oiPrismaV)
            } else {
                cbOiUbic.setSelectedItem('')
            }
            */
            //txtDICerca.setText(receta.diCercaR)
            txtAltOblea.setText(receta.altOblR)
            txtObservaciones.setText(receta.observacionesR)
            /*
            if ('l'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[0])
            } else if ('c'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[1])
            } else if ('b'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[2])
            } else if ('p'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[3])
            } else if ('i'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[4])
            } else if ('t'.equalsIgnoreCase(receta?.useGlasses)) {
                cbUso.setSelectedItem(uso[5])
            }
            */
        }
    }


    private void doOptSearch() {
        String input = txtEmpleado.text
        if (StringUtils.isNotBlank(input)) {
            String optometrista = CustomerController.findOptometrista(input)
            if (optometrista != null) {
                lblEmpleado.setText(optometrista)
            } else {
                sb.optionPane(message: "No existe el empleado", optionType: JOptionPane.DEFAULT_OPTION)
                        .createDialog(new JTextField(), "Error")
                        .show()
            }
        }
    }

    void doCancel() {
        //component.rec = rec
        //this.setVisible(false)
        this.dispose()

    }

    private String signoMas(String numero){

        if(numero.toDouble()>0.0){
            numero = '+' + numero
        }

        return numero

    }
    private String signoMenos(String numero){

          if(!numero.substring(0,1).equals('-')){
            numero = '-' + numero
          }

        return numero

    }

    private void useGlasess(){
         println(cbUso.selectedItem.toString().trim() + '   USO')
        if (cbUso.selectedItem.toString().trim().equals('LEJOS')) {
            receta.setUseGlasses('l')
        } else if (cbUso.selectedItem.toString().trim().equals('CERCA')) {
            receta.setUseGlasses('c')
        } else if (cbUso.selectedItem.toString().trim().equals('BIFOCAL')) {
            receta.setUseGlasses('b')
        } else if (cbUso.selectedItem.toString().trim().equals('PROGRESIVO')) {
            receta.setUseGlasses('p')
        } else if (cbUso.selectedItem.toString().trim().equals('INTERMEDIO')) {
            receta.setUseGlasses('i')
        } else if (cbUso.selectedItem.toString().trim().equals('BIFOCAL INTERMEDIO')) {
            receta.setUseGlasses('t')
        }

        receta.setOdEsfR(txtOdEsfera.text)
        receta.setOdCilR(txtOdCil.text)
        receta.setOdEjeR(txtOdEje.text)
        receta.setOdAdcR(txtOdAd.text)
       // receta.setOdAvR(txtOdAv.text)
        receta.setDiOd(txtOdDm.text)
       // receta.setOdPrismH(txtOdPrisma.text)
        receta.setDiLejosR(txtDILejos.text)
        receta.setOiAdcR(txtOiAd.text)
        receta.setOiEsfR(txtOiEsfera.text)
        receta.setOiCilR(txtOiCil.text)
        receta.setOiEjeR(txtOiEje.text)
       // receta.setOiAvR(txtOiAv.text)
        receta.setDiOi(txtOiDm.text)
        //receta.setOiPrismH(txtOiPrisma.text)
        //receta.setDiCercaR(txtDICerca.text)
        receta.setAltOblR(txtAltOblea.text)

        receta.setObservacionesR(txtObservaciones.text)
      //  receta.setOdPrismaV(cbOdUbic.selectedItem.toString() ?: '')
       // receta.setOiPrismaV(cbOiUbic.selectedItem.toString() ?: '')
        receta.setIdOpt(txtEmpleado.text)
        receta.setFolio(txtFolio.text)
        if (!receta?.idClient) {
            receta.setIdStore(idSucursal)
            receta.setIdClient(idCliente)
        }
        rec = CustomerController.saveRx(receta)
        doCancel()

    }

    private void doRxSave() {
        if (!StringUtils.trimToEmpty(txtEmpleado.text).isEmpty()
              && !StringUtils.trimToEmpty(txtFolio.text).isEmpty() ) {

                     String useGlass = cbUso.selectedItem.toString().trim()
                      println('UseGlass = ' + useGlass)
                      println('ItemUse = ' + itemUso)

    /*B*/   if(useGlass.equals(usoB[0])/*BIFOCAL*/ ){
                        useGlass = 'BIFOCAL'

                        if((itemUso != null) && (useGlass.equals(itemUso.trim()))){

                            /*
                            if( txtOdEsfera.text != '' &&
                                    txtOdCil.text != '' &&
                                    txtOdEje.text != '' &&
                                    txtOdAd.text  != '' &&
                                    txtDILejos.text   != '' &&



                                    txtOiEsfera.text  != '' &&
                                    txtOiCil.text  != '' &&
                                    txtOiEje.text  != '' &&
                                    txtOiAd.text != '' &&



                                    txtAltOblea.text  != ''
                            ){ */
                                useGlasess()
                           /* }else{
                                sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                        .createDialog(new JTextField(), "Error")
                                        .show()
                            }
                           */
                        } else{
                            sb.optionPane(message: "Receta: "+useGlass+" Articulo: "+itemUso, optionType: JOptionPane.DEFAULT_OPTION)
                                    .createDialog(new JTextField(), "Error")
                                    .show()
                        }
   /*P*/     }else if(useGlass.equals(usoP[0])/*PROGRESIVO*/){
                    useGlass = 'PROGRESIVO'

                    if((itemUso != null) && (useGlass.equals(itemUso.trim()))){
                 /*
                        if( txtOdEsfera.text != '' &&
                                txtOdCil.text != '' &&
                                txtOdEje.text != '' &&
                                txtOdAd.text  != '' &&

                                txtOdDm.text  != '' &&


                                txtOiEsfera.text  != '' &&
                                txtOiCil.text  != '' &&
                                txtOiEje.text  != '' &&
                                txtOiAd.text != '' &&
                                txtOiDm.text  != '' &&


                                txtAltOblea.text  != ''
                        ){
                    */        useGlasess()
                      /*  }else{
                            sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                    .createDialog(new JTextField(), "Error")
                                    .show()
                        }
                        */
                    } else{
                        sb.optionPane(message: "Receta: "+useGlass+" Articulo: "+itemUso, optionType: JOptionPane.DEFAULT_OPTION)
                                .createDialog(new JTextField(), "Error")
                                .show()
                    }

  /*SV*/    }else if(useGlass.equals(usoM[0])/*LEJOS*/ || useGlass.equals(usoM[1])/*CERCA*/  ){
                useGlass = 'MONOFOCAL'

                if((itemUso != null) && (useGlass.equals(itemUso.trim()))){
                   /*
                    if( txtOdEsfera.text != '' &&
                        txtOdCil.text != '' &&
                        txtOdEje.text != '' &&

                         txtDILejos.text   != '' &&
                         txtOiEsfera.text  != '' &&
                         txtOiCil.text  != '' &&
                         txtOiEje.text  != ''


                    ){ */
                        useGlasess()
              /*      }else{
                        sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                .createDialog(new JTextField(), "Error")
                                .show()
                    }   */
                    } else{
                        sb.optionPane(message: "Receta: "+useGlass+" Articulo: "+itemUso, optionType: JOptionPane.DEFAULT_OPTION)
                                .createDialog(new JTextField(), "Error")
                                .show()
                    }
            }

        } else {
            sb.optionPane(message: "Debe ingresar empleado y folio:", optionType: JOptionPane.DEFAULT_OPTION)
                    .createDialog(new JTextField(), "Error")
                    .show()
        }

    }


}