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
    private static JTextField sucursal = new JTextField()

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
                preferredSize: [240, 100],
                location: [ 200, 250 ]
        ) {
                 panel(layout: new MigLayout("wrap 4","[]10[][][]","[]20[]")) {

                     label(text: 'Ticket: ')
                     sucursal = textField(text:'12001',minimumSize: [70, 20])
                     label(text: '-')
                     factura = textField(minimumSize: [70, 20])

                     label()
                     button(text: 'Cancelar',actionPerformed: {doCancel()})
                     label()
                     button(text: 'Aceptar',actionPerformed: {doSave()})



                 }

        }

    }


    private void doSave(){
        OrderController.validaEntrega(factura.text,sucursal.text,false)
        doCancel()
    }

     private void doCancel(){
         this.setVisible(false)
     }





}