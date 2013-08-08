package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.model.FormaContacto
import mx.lux.pos.model.Jb
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.ui.controller.ContactController
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.model.UpperCaseDocument
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.BorderLayout
import java.awt.event.ItemEvent


class ContactDialog extends JDialog {

    private def sb

    private static JComboBox tipo
    private static List<String> tipos
    private static JComboBox dominio
    private static JLabel arroba
    private static List<String> dominios
    private static JTextField infoTipo
    private static JTextField correo
    private static JTextArea txtObservaciones
    private static NotaVenta nVenta

    ContactDialog(NotaVenta notaVenta) {

        sb = new SwingBuilder()
        nVenta = notaVenta
        tipos = CustomerController.findAllContactTypes()
        dominios = CustomerController.findAllCustomersDomains()

        buildUI()


    }


    void buildUI() {
        sb.dialog(this,
                title: 'Forma Contacto',
                resizable: false,
                pack: true,
                modal: true,
                preferredSize: [330, 225],
                layout: new MigLayout('wrap,center', '[fill,grow]'),
                location: [ 200, 250 ]
        ) {


                 panel(layout: new MigLayout("wrap 3","[][][]","[][]")) {
                     tipo = comboBox( items: tipos,itemStateChanged: typeChanged )
                     label()
                     infoTipo =  textField(minimumSize: [140, 20], visible: false)
                     correo =  textField(minimumSize: [140, 20])
                     arroba = label(text: '@')
                     dominio = comboBox( items: dominios )
                 }

            panel(layout: new MigLayout('wrap,center', '[fill,grow]')) {
                     scrollPane( border: titledBorder( title: 'Observaciones', ) ) {
                         txtObservaciones = textArea(document: new UpperCaseDocument(), lineWrap: true, minimumSize: [250, 50] )
                     }

            }
            panel(layout: new MigLayout('wrap 3', '[fill,grow][fill,grow][fill,grow]')) {

                     label()
                     button(text: 'Cancelar',actionPerformed: {doCancel()})
                     button(text: 'Aceptar',actionPerformed: {doSave()})

            }

        }

    }



    private void doSave(){

        Jb jb = ContactController.findJbxRX( nVenta?.factura)
          println('JBContacto: '+jb?.rx)
        FormaContacto fc = ContactController.findFCbyRx(jb?.rx)
           println('Fc: '+ fc?.rx)
        if( fc?.rx == null){
            FormaContacto  formaContacto = new FormaContacto()
             formaContacto.rx = jb?.rx
             formaContacto.id_cliente = nVenta?.idCliente
             formaContacto.fecha_mod = new Date()
             formaContacto.id_sucursal = nVenta?.idSucursal

             formaContacto?.id_tipo_contacto =   tipo?.selectedIndex + 1
            String contacto
            if( tipo?.selectedIndex == 0){
                contacto = correo?.text + '@' + dominio?.selectedItem?.toString()
            } else {
                contacto = infoTipo?.text

            }
            formaContacto?.contacto =  contacto

            formaContacto?.observaciones =  txtObservaciones?.text

            formaContacto = ContactController.saveFormaContacto(formaContacto)

            println('Forma Contacto: '+formaContacto?.rx)
        }

        doCancel()
    }

     private void doCancel(){
         this.setVisible(false)
     }

    private def typeChanged = { ItemEvent ev ->
        if ( ev.stateChange == ItemEvent.SELECTED ) {
            String typeName = ev.item

            if(typeName.trim()!='CORREO'){
                infoTipo.setVisible(true)
                correo.setVisible(false)
                arroba.setVisible(false)
                dominio.setVisible(false)
            }else{
                infoTipo.setVisible(false)
                correo.setVisible(true)
                arroba.setVisible(true)
                dominio.setVisible(true)
            }

        } else {

        }
    }



}

