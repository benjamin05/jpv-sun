package mx.lux.pos.ui.view

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.view.dialog.NoSaleDialog
import mx.lux.pos.ui.model.Customer
import mx.lux.pos.ui.model.Rx

import javax.swing.JPanel
import javax.swing.JFrame

import java.awt.BorderLayout
import mx.lux.pos.ui.resources.UI_Standards
import javax.swing.SwingUtilities
import mx.lux.pos.ui.controller.OpenSalesController
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

import java.awt.event.MouseEvent

class OpenDayDialogSample extends JFrame {

    private SwingBuilder sb = new SwingBuilder()
    private JPanel mainPanel
    private NoSaleDialog salesDialog

    OpenDayDialogSample() {
        buildUI()
    }

    // Internal Methods
    private void buildUI() {
        sb.build() {
            lookAndFeel('system')
            frame(this,
                    title: 'Sample Frame to trigger a dialog',
                    show: true,
                    pack: true,
                    resizable: true,
                    location: [100, 0],
                    preferredSize: [800, 600],
                    defaultCloseOperation: EXIT_ON_CLOSE
            ) {
                menuBar {
                    menu(text: "Archivo", mnemonic: "A") {
                        menuItem(text: "Salir", visible: true, actionPerformed: { println "Salir" })
                    }
                    menu(text: "Herramientas", mnemonic: "H") {
                        menuItem(text: "Apertura de Caja", visible: true,
                                actionPerformed: { OpenSalesController.instance.requestNewDay() }
                        )
                    }
                }
                panel() {
                    borderLayout()
                    panel(constraints: BorderLayout.PAGE_END) {
                        borderLayout()
                        panel(constraints: BorderLayout.LINE_END) {
                            button(text: "Launch",
                                    preferredSize: UI_Standards.BUTTON_SIZE,
                                    actionPerformed: { OpenSalesController.instance.requestNewDay() }
                            )
                            button(text: "Pantalla",
                                    preferredSize: UI_Standards.BUTTON_SIZE,
                                    actionPerformed: { onDialog() }
                            )
                        }
                    }
                }
            }
        }
    }


    protected void onDialog( ) {
        Customer customer = new Customer()
        customer.id = 1810
        customer.name = 'Pruebas de Sistemas'
        Rx receta = new Rx()
        salesDialog = new NoSaleDialog( 'Pepe Nador', this, receta, customer.id, 03, 'PROGRESIVO'  )
        salesDialog.activate()
    }

    // UI Response
    static void main(String[] args) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    void run() {
                        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring-config.xml")
                        ctx.registerShutdownHook()
                        OpenDayDialogSample sample = new OpenDayDialogSample()
                    }
                }
        )
    }


}
