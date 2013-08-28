package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.FeatureController
import mx.lux.pos.ui.model.Customer
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.panel.RXPanel
import mx.lux.pos.ui.view.panel.CustomerPanel

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Point
import javax.swing.JDialog
import javax.swing.JTabbedPane
import java.awt.Dimension

class NewCustomerAndRxDialog extends JDialog {

    private def sb = new SwingBuilder()

    private JTabbedPane tabbedPane
    private Component component
    private Customer cliente
    private boolean edit
    private Customer customer
    private CustomerPanel custPanel
    private RXPanel rxPanel
    private Boolean rxEnabled
    private Boolean canceled

    NewCustomerAndRxDialog( Component parent, Customer customer, boolean editar ) {
        component = parent
        cliente = customer
        edit = editar

        println('id cliente '+this.customer?.id)
        this.custPanel = new CustomerPanel( this, this.cliente, this.edit )
        rxEnabled = this.cliente?.id == null ? false : FeatureController.isRxEnabled()
         if ( rxEnabled ) {
           this.rxPanel = new RXPanel(custPanel.customer.id)
         } else {
           this.rxPanel = null
         }
        buildUI()

    }

    Customer getCustomer( ) {
        return customer
    }

    // UI Layout Definition
    void buildUI( ) {
        sb.dialog( this,
                title: "${cliente.fullName}",
                resizable: true,
                pack: true,
                modal: true,
                preferredSize: [ 650, 700 ] as Dimension,
                location: [ 90, 50 ] as Point,
        ) {
            borderLayout()
            tabbedPane( constraints: BorderLayout.CENTER ) {
                panel( this.custPanel, title: this.custPanel.title )
                if ( this.rxPanel != null ) {
                    panel( this.rxPanel, title: this.rxPanel.title )
                }
            }

            panel( constraints: BorderLayout.PAGE_END ) {
                borderLayout()
                button(
                        constraints: BorderLayout.LINE_END,
                        text: 'Cerrar',
                        preferredSize: UI_Standards.BUTTON_SIZE,
                        actionPerformed: { doCancel() }
                )
            }
        }
    }

    public void doCancel( ) {


        this.setVisible(false)


    }

    Boolean getCanceled( ) {
        return this.canceled
    }


}