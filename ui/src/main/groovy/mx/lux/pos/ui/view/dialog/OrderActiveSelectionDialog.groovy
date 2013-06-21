package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.ClienteProceso
import mx.lux.pos.ui.model.OrderActive
import mx.lux.pos.ui.resources.UI_Standards
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.table.TableRowSorter
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import javax.swing.*
import mx.lux.pos.model.NotaVenta



class OrderActiveSelectionDialog extends JDialog {

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

  OrderActiveSelectionDialog( ) {
    this.orderList = new ArrayList<OrderActive>()
    this.buildUI()
  }

  // Dialog Layout
  protected void buildUI( ) {
    sb.dialog( this,
        title: TXT_DIALOG_TITLE,
        location: [ 100, 150 ] as Point,
        preferredSize: [ 600, 320 ] as Dimension,
        resizable: false,
        modal: true,
        pack: true,
    ) {
      borderLayout()
      panel(border: BorderFactory.createEmptyBorder( 10, 10, 5, 10 ),
          constraints: BorderLayout.CENTER) {
        borderLayout()
        panel( constraints: BorderLayout.PAGE_START,
            border: BorderFactory.createEmptyBorder( 10, 10, 5, 10 )
        ) {
          borderLayout()
                  label( text: 'Cliente:', constraints: BorderLayout.LINE_START )
                  search =  textField(name: 'TxArea',
                          border:BorderFactory.createLineBorder(Color.gray),
                          preferredSize: [ 30, 20 ],
                          keyReleased:{onAlter(this.search)})
          lblInstructions = label( text: TXT_INSTRUCTIONS, constraints: BorderLayout.AFTER_LAST_LINE )
        }

        scrollPane( constraints: BorderLayout.CENTER ) {
          tOrders = table( selectionMode: ListSelectionModel.SINGLE_SELECTION,
              mouseClicked: { onOrderClick() } ) {
            model = tableModel( list: orderList ) {
              closureColumn( header: TXT_CUST_NAME_LABEL,
                  minWidth: 240,
                  read: { OrderActive o -> o.customerName }
              )
              closureColumn( header: TXT_PARTS_LABEL,
                  minWidth: 180,
                  read: { OrderActive o -> o.partList }
              )
              closureColumn( header: TXT_AMOUNT_LABEL,
                  maxWidth: 100,
                  read: { OrderActive o -> String.format( '$%,.2f', o.amount) }
              )
            } as DefaultTableModel
          }
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
         //     textArea(name: 'TxArea',
           //           border:BorderFactory.createEmptyBorder( 10, 10, 5, 10 ),
             //         keyPressed:{onAlter(this.model)} )
            button( 'Aceptar', preferredSize: UI_Standards.BUTTON_SIZE, actionPerformed: { onSelection() } )
            button( 'Cancelar', preferredSize: UI_Standards.BUTTON_SIZE, actionPerformed: { onCancel() } )
          }
        }
      }
    }
  }

  // Internal Methods

  // UI Management
  protected void updateUI( ) {
    this.model.fireTableDataChanged()
    this.lblInstructions.text = String.format( TXT_INSTRUCTIONS, ( orderList != null ? orderList.size() : 0 ) )
  }

  // Public methods
  void activate( ) {
    this.updateUI()
    this.selection = null
    this.setVisible( true )
  }

  OrderActive getOrderSelected( ) {
      return selection
  }

  void setCustomerList( List<ClienteProceso> pCustomerList ) {
    this.orderList.clear()
    for (ClienteProceso c : pCustomerList) {
      for (NotaVenta o : c.notaVentas) {
        if (o.detalles.size() > 0) {
          this.orderList.add( new OrderActive(o, c.cliente))
        }
      }
    }
    Collections.sort( this.orderList )
  }

  // Triggers
  private void onOrderClick( ) {

  }

    protected void onAlter(JTextField search){




       TableRowSorter<ListSelectionModel> sorter = new TableRowSorter<ListSelectionModel>(model)



        if (search.text.length() == 0) {
            sorter.setRowFilter(null)
        } else {

            sorter.setRowFilter(RowFilter.regexFilter(search.text.toUpperCase()))


        }




        tOrders.setRowSorter(sorter)




    }


    //  protected void onAlter(DefaultTableModel model){
  //      println('Correcto: '+ model.getColumnCount())
  //    println(model.columnList)
  //  }


  protected void onCancel( ) {
    selection = null
    this.setVisible( false )
  }

  protected void onSelection( ) {

      int index = tOrders.convertRowIndexToModel(tOrders.getSelectedRow())

    if (tOrders.selectedRowCount > 0) {
      this.logger.debug( String.format('Selected Row:%d', index )  )

      selection = this.orderList.getAt(index)

      this.setVisible( false )
    } else {
      this.logger.debug( 'No Row Selected' )
      sb.doLater {
        this.onCancel()
      }
    }
  }

}

