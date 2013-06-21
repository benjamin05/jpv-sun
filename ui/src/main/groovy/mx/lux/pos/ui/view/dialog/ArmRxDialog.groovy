package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.OrderController
import net.miginfocom.swing.MigLayout


import javax.swing.*
import java.awt.*


class ArmRxDialog extends JDialog {

    private def sb

    private Component component
    private static ButtonGroup material
    private static ButtonGroup acabado
    private static JTextField forma
    private static JRadioButton pasta
    private static JRadioButton opacado
    private static JRadioButton metal
    private static JRadioButton pulido
    private static JRadioButton nylon
    private static JRadioButton aire
    private static String idNotaV

    ArmRxDialog(Component parent, String idNotaVenta) {
        sb = new SwingBuilder()
        component = parent
        idNotaV = idNotaVenta
        buildUI()

    }


    void buildUI() {
        sb.dialog(this,
                title: 'Armazon',
                resizable: false,
                pack: true,
                modal: true,
                preferredSize: [165, 235],
                location: [ 200, 250 ]
        ) {
                 panel(layout: new MigLayout("wrap 2","[]20[]","[][][][][]20[][]")) {
                     material =   buttonGroup()
                     acabado = buttonGroup()

                         label(text: 'Material'  )
                         label(text: 'Acabado')

                   pasta = radioButton(text:"Pasta", buttonGroup:material)
                     pasta.setSelected(true)
                     pasta.setActionCommand('Pasta')
                     opacado =  radioButton(text:"Opacado", buttonGroup:acabado)
                      opacado.setSelected(true)
                      opacado.setActionCommand("Opacado")
                    metal = radioButton(text:"Metal", buttonGroup:material)
                     metal.setActionCommand('Metal')
                    pulido = radioButton(text:"Pulido", buttonGroup:acabado)
                    pulido.setActionCommand('Pulido')
                    nylon = radioButton(text:"Nylon", buttonGroup:material)
                    nylon.setActionCommand('Nylon')
                     label()

                    aire = radioButton(text:"Aire", buttonGroup:material)
                    aire.setActionCommand('Aire')
                     label()

                     label(text: 'Forma')
                    forma = textField(minimumSize: [70, 20])
                     label()
                     button(text: 'Aceptar',actionPerformed: {doSave()})



                 }

        }

    }

    private void doSave(){

      String opciones =  material.selection.actionCommand +', '+acabado.selection.actionCommand
        String form = forma.text
      OrderController.saveFrame(idNotaV,opciones,form)

        doCancel()
    }

     private void doCancel(){
         this.setVisible(false)
     }





}