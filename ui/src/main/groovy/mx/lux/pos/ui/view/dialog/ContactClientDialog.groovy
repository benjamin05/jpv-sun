package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.ClienteProceso
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.ui.model.OrderActive
import mx.lux.pos.ui.resources.UI_Standards
import net.miginfocom.swing.MigLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.*
import javax.swing.table.TableRowSorter
import java.awt.*
import java.util.List



class ContactClientDialog extends JDialog {

  private static String TXT_DIALOG_TITLE = 'Seleccionar Cliente Caja'
  private static String TXT_INSTRUCTIONS = '%d ordenes pendientes de pago.'
  private static String TXT_CUST_NAME_LABEL = 'Cliente'
  private static String TXT_PARTS_LABEL = 'Articulos'
  private static String TXT_AMOUNT_LABEL = 'Monto'

  private SwingBuilder sb = new SwingBuilder()
  private Logger logger = LoggerFactory.getLogger( this.getClass() )

  private List<OrderActive> orderList
  private OrderActive selection
  private JLabel lblInstructions
  private JTable tOrders
  private DefaultTableModel model
  private JTextField search

    ContactClientDialog( ) {
    this.orderList = new ArrayList<OrderActive>()
    this.buildUI()
  }

  // Dialog Layout
  protected void buildUI( ) {
    sb.dialog( this,
        title: 'Contactos',
        location: [ 100, 150 ] as Point,
        preferredSize: [ 400, 200 ] as Dimension,
        resizable: true,
        modal: true,
        layout: new MigLayout('wrap,center', '[fill,grow]'),
        pack: true,
    ) {
        panel(layout: new MigLayout('wrap 3', '[fill,grow][fill,grow][fill,grow]')) {
           label()
            label()
            button( '+', preferredSize: UI_Standards.BUTTON_SIZE, actionPerformed: { addContact() } )
        }

        panel(layout: new MigLayout('wrap,center', '[fill,grow]')) {
        scrollPane( constraints: BorderLayout.CENTER ) {
          tOrders = table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
            model = tableModel( list: orderList ) {
              closureColumn( header: 'Tipo de Contacto',
                  minWidth: 240,
                  read: { OrderActive o -> o.customerName }
              )
              closureColumn( header: 'Dato',
                  minWidth: 180,
                  read: { OrderActive o -> o.partList }
              )
            } as DefaultTableModel
          }
        }
        }

        panel(layout: new MigLayout('wrap 3', '[fill,grow][fill,grow][fill,grow]')) {
            label()
            button( 'Aceptar', preferredSize: UI_Standards.BUTTON_SIZE, actionPerformed: { onSelection() } )
            button( 'Cancelar', preferredSize: UI_Standards.BUTTON_SIZE, actionPerformed: { onCancel() } )
        }
    }
  }


  protected void onCancel( ) {
    this.setVisible( false )
  }

  protected void onSelection( ) {

  }

    protected void addContact(){}


}

