package mx.lux.pos.ui.view.component

import mx.lux.pos.ui.model.ContactType
import mx.lux.pos.ui.model.GenderType
import mx.lux.pos.ui.model.UpperCaseDocument
import mx.lux.pos.ui.view.verifier.NotEmptyVerifier
import net.miginfocom.swing.MigLayout

import java.awt.BorderLayout
import javax.swing.JComboBox
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.JPanel

class CustomerPanelForeign extends CustomerPanelStatistics {

  protected static final String TXT_COUNTRY_LABEL = 'País'
  protected JTextField txtCountry

  CustomerPanelForeign() {
    super()
  }

  // UI Layout
  JPanel buildAddressPanel() {
    cmbState = null
    JPanel panel = sb.panel( border:sb.titledBorder( 'Dirección' ),
        layout: new MigLayout( 'wrap 2', '[][fill,grow]150!' ),
    ) {
      label( TXT_CITY_LABEL )
      txtCity = textField( document: new UpperCaseDocument() )

      label( TXT_COUNTRY_LABEL )
      txtCountry = textField( document: new UpperCaseDocument() )

    }
    return panel
  }


}
