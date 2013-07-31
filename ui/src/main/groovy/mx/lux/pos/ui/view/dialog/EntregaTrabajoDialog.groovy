package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.model.Order
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class EntregaTrabajoDialog extends JDialog {

    private def sb

    private Component component

    private static JTextField factura = new JTextField()


    EntregaTrabajoDialog(Component parent) {

        sb = new SwingBuilder()
        component = parent
        buildUI()


    }


    void buildUI() {
        sb.dialog(this,
                title: 'Entrega Trabajo',
                resizable: false,
                pack: true,
                modal: true,
                preferredSize: [180, 100],
                location: [ 200, 250 ]
        ) {
                 panel(layout: new MigLayout("wrap 2","[]10[]","[]20[]")) {

                     label(text: 'Factura: ')
                     factura = textField(minimumSize: [70, 20])
                     button(text: 'Cancelar',actionPerformed: {doCancel()})
                     button(text: 'Aceptar',actionPerformed: {doSave()})



                 }

        }

    }


    private void doSave(){
        OrderController.validaEntrega(factura.text,false)
        doCancel()
    }

     private void doCancel(){
         this.setVisible(false)
     }





}