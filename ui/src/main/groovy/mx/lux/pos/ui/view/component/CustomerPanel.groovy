package mx.lux.pos.ui.view.component

import javax.swing.JPanel
import mx.lux.pos.ui.model.Customer
import groovy.swing.SwingBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.model.DefaultTableModel
import mx.lux.pos.ui.model.Rx
import mx.lux.pos.ui.model.CustomerType

abstract class CustomerPanel extends JPanel implements IView {

  private static final String TXT_CUSTOMER_TAB = 'Cliente'

  protected SwingBuilder sb = new SwingBuilder()
  protected Logger logger = LoggerFactory.getLogger(this.getClass())

  private Customer customer
  private Boolean uiEnabled

  CustomerPanel() {
    this.buildUI()
  }

  // UI Layout
  protected abstract void buildUI( )

  // Public methods
  Customer getCustomer() {
    return this.customer
  }

  Boolean isUIEnabled() {
    return this.uiEnabled
  }

  void setCustomer(Customer pCustomer) {
    this.customer = pCustomer
    this.refreshUI()
  }

  String getName() {
    return TXT_CUSTOMER_TAB
  }

  Boolean isCustomerDomestic( ) {
    return CustomerType.DOMESTIC.equals( this.getCustomer().type )
  }

  // UI Management
  abstract void assign()
  abstract void refreshUI()
  abstract Boolean validateInput()

  void disableUI() {
    this.uiEnabled = false
  }
  void enableUI() {
    this.uiEnabled = true
  }


}
