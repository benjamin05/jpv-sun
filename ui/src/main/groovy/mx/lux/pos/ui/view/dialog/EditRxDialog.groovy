package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.Rx
import mx.lux.pos.model.Receta
import net.miginfocom.swing.MigLayout

import java.awt.Component
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel

import org.apache.commons.lang3.StringUtils
import mx.lux.pos.ui.controller.CustomerController
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

    private JTextField txtObservaciones


    private JPanel empleadoPanel
    private final double VALOR_MULTIPLO = 0.25;
    private boolean mostrarParametroSV = true
    private boolean mostrarParametroPB = true

    private static String itemUso = null
    private static String limpiarAux
    private static String idNotaV
    List<String> ubicacion = ["", "ARRIBA", "ABAJO", "AFUERA", "ADENTRO"]
    List<String> uso = ["LEJOS", "CERCA", "BIFOCAL", "PROGRESIVO", "INTERMEDIO", "BIFOCAL INTERMEDIO"]

    Boolean canceled
    String Title

    EditRxDialog(Component parent, Rx receta, Integer idCliente, Integer idSucursal, String titulo,String uso,String idNotaVenta ) {

        sb = new SwingBuilder()
        component = parent
        itemUso =  uso
        rec = null
        idNotaV = idNotaVenta
        title = titulo
        if(itemUso.trim().equals('MONOFOCAL'))
        {
            mostrarParametroSV = true
            mostrarParametroPB = false
        }else{
            mostrarParametroSV = false
            mostrarParametroPB = true
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
                resizable: true,
                pack: true,
                modal: true,
                preferredSize: [680, 360],
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
            }
            panel( layout: new MigLayout( 'fill,wrap 3','[fill][fill][fill,grow]' )){
                label(text: 'Uso')
                cbUso = comboBox(items: uso)
                label()
            }

            panel(border: titledBorder("Rx"), layout: new MigLayout('fill,wrap ,center', '[fill,grow]')) {
                panel(layout: new MigLayout('fill,wrap 11,center',
                        '''[center][fill,grow,center][fill,grow,center][fill,grow,center][fill,grow,center][fill,grow,center]
                            [fill,grow,center][fill,grow,center][fill,grow,center]30[center][fill,grow,center]''')) {
                    label()

                    label(text: 'Esfera', horizontalAlignment: JTextField.CENTER )
                    label(text: 'Cil.', toolTipText: 'Cilindro', horizontalAlignment: JTextField.CENTER )
                    label(text: 'Eje', horizontalAlignment: JTextField.CENTER )
                    label(text: 'Ad.', toolTipText: 'Adición', horizontalAlignment: JTextField.CENTER )


                    label(text: 'A.V.', toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.CENTER,visible:false,enabled:false )
                    label(text: 'D.M.', toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.CENTER,visible:mostrarParametroPB,enabled:mostrarParametroPB  )
                    label(text: 'Prisma', horizontalAlignment: JTextField.CENTER,visible:false,enabled:false )
                   label(text: 'Ubic.', toolTipText: 'Ubicación', horizontalAlignment: JTextField.CENTER,visible:false,enabled:false )

                    label(text: 'D.I. Lejos', toolTipText: 'Distancia Interpupilar Lejos',visible:mostrarParametroSV ,enabled:mostrarParametroSV)
                    txtDILejos = textField(minimumSize: [20, 20], toolTipText: 'Distancia Interpupilar Lejos', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroSV ,enabled:mostrarParametroSV)
                    txtDILejos.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtDILejos)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtDILejos,90,45,1,'0')
                        }
                    })
                    label(text: 'O.D.', toolTipText: 'Ojo Derecho')
                    txtOdEsfera = textField( horizontalAlignment: JTextField.RIGHT )
                    txtOdEsfera.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdEsfera)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdEsfera,35,-35,0.25,'.00')

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
                            validacion(txtOdCil,12,-12,0.25,'.00')
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
                                validacion(txtOdEje,180,0,1,'0')
                            }
                    })

                    txtOdAd = textField( toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT )
                    txtOdAd.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                             limpiar(txtOdAd)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdAd,4,0.75,0.25,'.00')
                    }
                    })
                    txtOdAv = textField( toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT,visible:false,enabled:false )
                    txtOdAv.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            txtOdAv.setSelectionStart(3)
                            txtOdAv.setSelectionEnd(5)
                        }

                        @Override
                        void focusLost(FocusEvent e) { }
                    })
                    txtOdDm = textField( toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroPB,enabled:mostrarParametroPB)
                    txtOdDm.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdDm)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdDm,45,22,0.1,'.0')
                        }
                    })
                    txtOdPrisma = textField( horizontalAlignment: JTextField.RIGHT,visible:false,enabled:false)
                    txtOdPrisma.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOdPrisma)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOdPrisma,12,0,0.25,'.00')
                        }
                    })
                    cbOdUbic = comboBox(items: ubicacion, toolTipText: 'Ubicación',visible:false,enabled:false)
                    label(text: 'D.I. Cerca', toolTipText: 'Distancia Interpupilar Cerca',visible:mostrarParametroSV ,enabled:mostrarParametroSV)
                    txtDICerca = textField( minimumSize: [20, 20], toolTipText: 'Distancia Interpupilar Cerca', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroSV ,enabled:mostrarParametroSV)
                    txtDICerca.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtDICerca)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtDICerca,90,45,1,'0')
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
                            validacion(txtOiEsfera,35,-35,0.25,'.00')

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
                            validacion(txtOiCil,12,-12,0.25,'.00')
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
                            validacion(txtOiEje,180,0,1,'0')
                        }
                    })

                    txtOiAd = textField( toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT )
                    txtOiAd.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiAd)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiAd,4,0.75,0.25,'.00')
                        }
                    })
                    txtOiAv = textField( toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT,visible:false,enabled:false)
                    txtOiAv.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            txtOiAv.setSelectionStart(3)
                            txtOiAv.setSelectionEnd(5)
                        }

                        @Override
                        void focusLost(FocusEvent e) { }
                    })
                    txtOiDm = textField( toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroPB,enabled:mostrarParametroPB )
                        txtOiDm.addFocusListener(new FocusListener() {
                            @Override
                            void focusGained(FocusEvent e) {
                                limpiar(txtOiDm)
                            }

                            @Override
                            void focusLost(FocusEvent e) {
                                validacion(txtOiDm,45,22,0.1,'.0')
                            }
                        })
                    txtOiPrisma = textField( horizontalAlignment: JTextField.RIGHT,visible:false,enabled:false)
                    txtOiPrisma.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtOiPrisma)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtOiPrisma,12,0,0.25,'.00')
                        }
                    })
                    cbOiUbic = comboBox(items: ubicacion, toolTipText: 'Ubicación',visible:false,enabled:false)
                    label(text: 'Alt. Oblea', toolTipText: 'Altura Oblea',visible:mostrarParametroPB,enabled:mostrarParametroPB)
                    txtAltOblea = textField( minimumSize: [20, 20], toolTipText: 'Altura Oblea', horizontalAlignment: JTextField.RIGHT,visible:mostrarParametroPB,enabled:mostrarParametroPB )
                    txtAltOblea.addFocusListener(new FocusListener() {
                        @Override
                        void focusGained(FocusEvent e) {
                            limpiar(txtAltOblea)
                        }

                        @Override
                        void focusLost(FocusEvent e) {
                            validacion(txtAltOblea,40,10,0.5,'.00')
                        }
                    })

                }
                panel(layout: new MigLayout('fill,wrap 2', '[fill][fill,grow]')) {
                    label(text: 'Observaciones:')
                    txtObservaciones = textField(document: new UpperCaseDocument())
                }
            }
            panel(layout: new MigLayout('wrap 2,right', '[right][right]')) {
                button(text: 'Guardar', actionPerformed: {doRxSave()}, maximumSize: [110, 90])
                button(text: 'Cancelar', actionPerformed: {doCancel()}, maximumSize: [110, 90])
            }
        }
    }

    private void limpiar(JTextField txtField){
        limpiarAux = txtField.text
        txtField.text = ''

    }

    private void validacion(JTextField txtField, double max, double min, double interval, String format){
        if (txtField.text.trim().length() > 0 || txtField.text.trim() == '0') {
            double number
            String txt = txtField.text.trim()

            txtField.text = ''
            Double multiplo = 0.0;
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

                            txtField.text = txt






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
            if (receta.odAvR != null) {
                txtOdAv.setText(receta.odAvR)
            } else {
                txtOdAv.setText("20/")
            }
            txtOdDm.setText(receta.diOd)
            txtOdPrisma.setText(receta.odPrismH)
            txtDILejos.setText(receta.diLejosR)
            txtOiEsfera.setText(receta.oiEsfR)
            txtOiCil.setText(receta.oiCilR)
            txtOiEje.setText(receta.oiEjeR)
            if (receta.oiAvR != null) {
                txtOiAv.setText(receta.oiAvR)
            } else {
                txtOiAv.setText("20/")
            }
            txtOiDm.setText(receta.diOi)
            txtOiPrisma.setText(receta.oiPrismH)
            if (receta.odPrismaV != null) {
                cbOdUbic.setSelectedItem(receta.odPrismaV)
            } else {
                cbOdUbic.setSelectedItem('')
            }
            if (receta.oiPrismaV != null) {
                cbOiUbic.setSelectedItem(receta.oiPrismaV)
            } else {
                cbOiUbic.setSelectedItem('')
            }
            txtDICerca.setText(receta.diCercaR)
            txtAltOblea.setText(receta.altOblR)
            txtObservaciones.setText(receta.observacionesR)

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

        component.rec = rec

        this.setVisible(false)
        this.dispose()

    }

    private String signo(String numero){

        if(numero.toDouble()>0.0){
            numero = '+' + numero
        }

        return numero

    }

    private void useGlasess(){
         println(cbUso.selectedItem.toString().trim() + '   USO')
        if (cbUso.selectedItem.toString().trim().equals(uso[0])) {
            receta.setUseGlasses('l')
        } else if (cbUso.selectedItem.toString().trim().equals(uso[1])) {
            receta.setUseGlasses('c')
        } else if (cbUso.selectedItem.toString().trim().equals(uso[2])) {
            receta.setUseGlasses('b')
        } else if (cbUso.selectedItem.toString().trim().equals(uso[3])) {
            receta.setUseGlasses('p')
        } else if (cbUso.selectedItem.toString().trim().equals(uso[4])) {
            receta.setUseGlasses('i')
        } else if (cbUso.selectedItem.toString().trim().equals(uso[5])) {
            receta.setUseGlasses('t')
        }

        receta.setOdEsfR(signo(txtOdEsfera.text))
        receta.setOdCilR(signo(txtOdCil.text))
        receta.setOdEjeR(txtOdEje.text)
        receta.setOdAdcR(txtOdAd.text)
        receta.setOdAvR(txtOdAv.text)
        receta.setDiOd(txtOdDm.text)
        receta.setOdPrismH(txtOdPrisma.text)
        receta.setDiLejosR(txtDILejos.text)

        receta.setOiEsfR(signo(txtOiEsfera.text))
        receta.setOiCilR(signo(txtOiCil.text))
        receta.setOiEjeR(txtOiEje.text)
        receta.setOiAvR(txtOiAv.text)
        receta.setDiOi(txtOiDm.text)
        receta.setOiPrismH(txtOiPrisma.text)
        receta.setDiCercaR(txtDICerca.text)
        receta.setAltOblR(txtAltOblea.text)

        receta.setObservacionesR(txtObservaciones.text)
        receta.setOdPrismaV(cbOdUbic.selectedItem.toString() ?: '')
        receta.setOiPrismaV(cbOiUbic.selectedItem.toString() ?: '')
        receta.setIdOpt(txtEmpleado.text)
        receta.setFolio(txtFolio.text)
        if (!receta?.idClient) {
            receta.setIdStore(idSucursal)
            receta.setIdClient(idCliente)
        }


        rec = CustomerController.saveRx(receta)

        ArmRxDialog armazon = new ArmRxDialog(this,idNotaV)
        armazon.show()

        doCancel()

    }

    private void doRxSave() {
        if (!StringUtils.trimToEmpty(txtEmpleado.text).isEmpty()
              && !StringUtils.trimToEmpty(txtFolio.text).isEmpty() ) {

                     String useGlass = cbUso.selectedItem.toString().trim()


    /*B*/   if(useGlass.equals(uso[2])/*BIFOCAL*/ || useGlass.equals(uso[5])/*BIFOCAL INTERMEDIO*/ ){
                        useGlass = 'BIFOCAL'

                        if((itemUso != null) && (useGlass.equals(itemUso.trim()))){

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
                                useGlasess()
                            }else{
                                sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                        .createDialog(new JTextField(), "Error")
                                        .show()
                            }
                        } else{
                            sb.optionPane(message: "Receta: "+useGlass+" Articulo: "+itemUso, optionType: JOptionPane.DEFAULT_OPTION)
                                    .createDialog(new JTextField(), "Error")
                                    .show()
                        }
   /*P*/     }else if(useGlass.equals(uso[3])/*PROGRESIVO*/){
                    useGlass = 'PROGRESIVO'

                    if((itemUso != null) && (useGlass.equals(itemUso.trim()))){

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
                            useGlasess()
                        }else{
                            sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                    .createDialog(new JTextField(), "Error")
                                    .show()
                        }
                    } else{
                        sb.optionPane(message: "Receta: "+useGlass+" Articulo: "+itemUso, optionType: JOptionPane.DEFAULT_OPTION)
                                .createDialog(new JTextField(), "Error")
                                .show()
                    }

  /*SV*/    }else if(useGlass.equals(uso[0])/*LEJOS*/ || useGlass.equals(uso[1])/*CERCA*/ || useGlass.equals(uso[4])/*INTERMEDIO*/ ){
                useGlass = 'MONOFOCAL'

                if((itemUso != null) && (useGlass.equals(itemUso.trim()))){

                    if( txtOdEsfera.text != '' &&
                        txtOdCil.text != '' &&
                        txtOdEje.text != '' &&
                        txtOdAd.text  != '' &&
                         txtDILejos.text   != '' &&
                         txtOiEsfera.text  != '' &&
                         txtOiCil.text  != '' &&
                         txtOiEje.text  != '' &&
                         txtOiAd.text != '' &&
                        txtDICerca.text  != ''

                    ){
                        useGlasess()
                    }else{
                        sb.optionPane(message: "Llenar todos los campos", optionType: JOptionPane.DEFAULT_OPTION)
                                .createDialog(new JTextField(), "Error")
                                .show()
                    }
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