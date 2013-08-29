package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.controller.FeatureController
import mx.lux.pos.ui.model.Customer
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.panel.RXPanel
import mx.lux.pos.ui.view.panel.CustomerPanel

import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Point
import javax.swing.JDialog
import javax.swing.JTabbedPane
import java.awt.Dimension
import java.awt.event.MouseEvent

class NewCustomerAndRxDialog extends JDialog {

    private def sb = new SwingBuilder()

    private JTabbedPane tabbedPane
    private Component component
    private Customer cliente
    private boolean edit
    private Customer customer
    private CustomerPanel custPanel
    private RXPanel rxPanel
    private JPanel storePanel
    private Boolean rxEnabled
    private Boolean canceled
    private JTabbedPane pestanias

    NewCustomerAndRxDialog( Component parent, Customer customer, boolean editar ) {
        component = parent
        cliente = customer
        edit = editar

        this.custPanel = new CustomerPanel( this, this.cliente, this.edit )
        rxEnabled = this.cliente?.id == null ? false : FeatureController.isRxEnabled()
         if ( rxEnabled ) {
           this.rxPanel = new RXPanel(custPanel.customer.id)
           this.storePanel = new JPanel()
         } else {
           this.rxPanel = null
         }
        buildUI()

    }

    Customer getCustomer( ) {
        return cliente
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
            pestanias = tabbedPane( constraints: BorderLayout.CENTER  ) {
                panel( this.custPanel, title: this.custPanel.title )
                if ( this.rxPanel != null ) {
                    panel( this.rxPanel, title: this.rxPanel.title )
                    panel( this.storePanel, title: 'Ventas' )
                }

            }
            pestanias.addChangeListener(new ChangeListener() {
                @Override
                void stateChanged(ChangeEvent e) {
                    if(pestanias.selectedComponent.equals(storePanel)){
                      canceled = true
                      CustomerController.addClienteProceso(cliente)
                      doCancel()
                    }
                }
            })

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

    /*private def onSalesClick = { MouseEvent ev ->
      if ( SwingUtilities.isLeftMouseButton( ev ) ) {
        canceled = true
        this.setVisible(false)
      }
    }*/

}