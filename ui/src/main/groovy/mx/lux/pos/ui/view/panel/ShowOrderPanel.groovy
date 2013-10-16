package mx.lux.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.Jb
import mx.lux.pos.model.Pago
import mx.lux.pos.ui.controller.AccessController
import mx.lux.pos.ui.controller.CancellationController
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.model.IPromotion
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.model.OperationType
import mx.lux.pos.ui.model.Order
import mx.lux.pos.ui.model.OrderItem
import mx.lux.pos.ui.model.Payment
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.dialog.AuthorizationDialog
import mx.lux.pos.ui.view.dialog.CancellationDialog
import mx.lux.pos.ui.view.dialog.PaymentDialog
import mx.lux.pos.ui.view.dialog.RefundDialog
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.text.NumberFormat
import java.util.List

class ShowOrderPanel extends JPanel {


  private SwingBuilder sb
  private Order order
  private JButton customerName

    private JButton ppButton
  private JButton cancelButton
  private JButton returnButton
  private JButton printReturnButton
  private JTextArea comments
  private DefaultTableModel itemsModel
  private DefaultTableModel paymentsModel
  private DefaultTableModel dealsModel
  private JLabel folio
  private JLabel bill

    private JLabel status

    private JLabel fechaE
    private JLabel empIDE



  private JLabel date
  private JLabel employee
  private JLabel total
  private JLabel paid
  private JLabel due
  private JPanel navigatorPanel
  private BigDecimal sumaPagos = BigDecimal.ZERO
  private static final BigDecimal montoCentavos = BigDecimal.ZERO
  private List<IPromotion> lstPromociones = new ArrayList<IPromotion>()
  private JScrollPane pago
  private Pago pagoN

  private static final String DATE_FORMAT = 'dd-MM-yyyy'
  private static final String GENERICO_ARMAZON = 'A'
  private static final String TAG_SURTE_SUCURSAL = 'S'
  private static final String TAG_SURTE_PINO = 'P'


  ShowOrderPanel( ) {
    sb = new SwingBuilder()
    order = new Order()
    buildUI()
    doBindings()
    //prom = [ ] as ObservableList
    //prom.addAll( CancellationController.findPromotionsbyidFactura( order.id ) )
  }

  private void buildUI( ) {
    sb.panel( this, layout: new MigLayout( 'insets 5,fill,wrap', '[fill]', '[fill]' ) ) {
      panel( layout: new MigLayout( 'insets 0,fill', '[fill,300][fill,200][fill,240!]', '[fill]' ) ) {
        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][fill,220!]', '[top]' ) ) {
          label( 'Cliente' )
          customerName = button( enabled: false )

          navigatorPanel = panel( constraints: 'skip' )
        }

        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][grow,right]', '[top]' ) ) {

          label( 'Factura' )
          bill = label()
          label( 'Fecha' )
          date = label()
          employee = label( constraints: 'span 2', maximumSize: [ 210, 30 ] )
            status = label(  foreground: UI_Standards.NORMAL_FOREGROUND, visible: false )
            fechaE = label(foreground: UI_Standards.NORMAL_FOREGROUND, visible: false )
            empIDE = label(constraints: 'span 2,hidemode 3', maximumSize: [ 210, 30 ] ,foreground: UI_Standards.NORMAL_FOREGROUND, visible: false )

        }

        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][grow,right]', '[top]' ) ) {
          def displayFont = new Font( '', Font.BOLD, 22 )
          label( 'Venta' )
          total = label( font: displayFont )
          label( 'Pagado' )
          paid = label( font: displayFont )
          label( 'Saldo' )
          due = label( font: displayFont )
        }
      }

      scrollPane( border: titledBorder( title: 'Art\u00edculos' ) ) {
        table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
          itemsModel = tableModel( list: order.items ) {
            closureColumn(
                header: 'Art\u00edculo',
                read: {OrderItem tmp -> "${tmp?.item?.name} ${tmp?.item?.color ?: ''}"},
                minWidth: 80,
                maxWidth: 100
            )
            closureColumn(
                header: 'Descripci\u00f3n',
                read: {OrderItem tmp -> tmp?.description}
            )
            closureColumn(
                header: 'Cantidad',
                read: {OrderItem tmp -> tmp?.quantity},
                minWidth: 70,
                maxWidth: 70
            )
            closureColumn(
                header: 'Precio',
                read: {OrderItem tmp -> tmp?.item?.price},
                minWidth: 80,
                maxWidth: 100,
                cellRenderer: new MoneyCellRenderer()
            )
            closureColumn(
                header: 'Total',
                read: {OrderItem tmp -> tmp?.item?.price * tmp?.quantity},
                minWidth: 80,
                maxWidth: 100,
                cellRenderer: new MoneyCellRenderer()
            )
          } as DefaultTableModel
        }
      }

      panel( layout: new MigLayout( 'insets 0,fill', '[fill][fill,240!]', '[fill]' ) ) {

        scrollPane( border: titledBorder( title: 'Promociones' ) ) {
          table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
            dealsModel = tableModel( list: lstPromociones ) {
              closureColumn( header: 'Descripci\u00f3n', read: {IPromotion tmp -> tmp?.descripcion} )
              closureColumn( header: 'Art\u00edculo', read: {IPromotion tmp -> tmp?.articulo}, maxWidth: 100 )
              closureColumn( header: 'Precio Lista', read: {IPromotion tmp -> tmp?.precioLista}, maxWidth: 80, cellRenderer: new MoneyCellRenderer() )
              closureColumn( header: 'Descuento', read: {IPromotion tmp -> tmp?.descuento}, maxWidth: 80, cellRenderer: new MoneyCellRenderer() )
              closureColumn( header: 'Precio Neto', read: {IPromotion tmp -> tmp?.precioNeto}, maxWidth: 80, cellRenderer: new MoneyCellRenderer() )
            } as DefaultTableModel
          }
        }

        pago = scrollPane( border: titledBorder( title: 'Pagos' ) ) {
          table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
            paymentsModel = tableModel( list: order.payments ) {
              closureColumn( header: 'Descripci\u00f3n', read: {Payment tmp -> tmp?.description} )
              closureColumn( header: 'Monto', read: {Payment tmp -> tmp?.amount}, maxWidth: 100, cellRenderer: new MoneyCellRenderer() )
            } as DefaultTableModel
          }
        }
      }

      scrollPane( border: titledBorder( title: 'Observaciones' ) ) {
        comments = textArea( lineWrap: true, enabled: false )
      }

      panel( layout: new MigLayout( 'insets 0,right', '[grow,fill,145!]', '[grow,fill,40!]' ) ) {
        cancelButton = button( 'Cancelar', actionPerformed: doCancel, constraints: 'hidemode 3' )
        returnButton = button( 'Devoluci\u00f3n', actionPerformed: doRefund, constraints: 'hidemode 3' )
        printReturnButton = button( 'Imprimir Cancelaci\u00f3n', actionPerformed: doPrintRefund, constraints: 'hidemode 3' )

          ppButton = button( 'Pagar', actionPerformed: doSwitchPP  )

      }
    }
    navigatorPanel.add( new OrderNavigatorPanel( order, {doBindings()} ) )
  }

  private void doBindings( ) {

    sb.build {
      bean( customerName, text: bind {order.customer?.fullName} )
      //bean( folio, text: bind {order.id} )
      bean( bill, text: bind {order.bill} )
      bean( employee, text: bind {order.employee} )
      //bean( status )
      //bean( status )
        if(order.fechaEntrega != null){
            bean( status, text: 'ENTREGADA',foreground: UI_Standards.NORMAL_FOREGROUND, visible: bind { order.fechaEntrega != null} )
        }
        if('T'.equalsIgnoreCase( order.status )){
            bean( status, text: 'CANCELADA',foreground: UI_Standards.WARNING_FOREGROUND,visible: bind {'T'.equalsIgnoreCase( order.status )} )
        }
        bean( fechaE, visible: bind { order.fechaEntrega != null && !'T'.equalsIgnoreCase( order.status )} )
        bean( fechaE, text: bind( source: order, sourceProperty: 'fechaEntrega', converter: dateConverter ) )
        bean( empIDE, visible: bind { order.fechaEntrega != null && !'T'.equalsIgnoreCase( order.status )} )
        bean( empIDE, text: bind( source: order, sourceProperty: 'employee' ) )
      bean( date, text: bind( source: order, sourceProperty: 'date', converter: dateConverter ) )
      bean( total, text: bind( source: order, sourceProperty: 'total', converter: currencyConverter ) )
      bean( paid, text: bind( source: order, sourceProperty: 'paid', converter: currencyConverter ) )
      bean( due, text: bind( source: order, sourceProperty: 'due', converter: currencyConverter ) )
      bean( itemsModel.rowsModel, value: bind( source: order, sourceProperty: 'items', mutual: true ) )
      bean( paymentsModel.rowsModel, value: bind( source: order, sourceProperty: 'payments', mutual: true ) )
      lstPromociones.clear()
      for( IPromotion promotion : order.deals ){
        lstPromociones.add( promotion )
      }
      bean( comments, text: bind( source: order, sourceProperty: 'comments', mutual: true ) )
      bean( cancelButton, visible: bind {!'T'.equalsIgnoreCase( order.status )} )
      sumaPagos = BigDecimal.ZERO
      for ( Payment payment : order.payments ) {
        println(payment?.amount)
      try{
        sumaPagos = sumaPagos.add( payment.refundable )
      }catch(e){
          sumaPagos=sumaPagos
      }
      }
      bean( returnButton, visible: bind {( 'T'.equalsIgnoreCase( order.status ) ) && ( sumaPagos.compareTo( montoCentavos ) > 0 ) } )
      bean( printReturnButton, visible: bind {( 'T'.equalsIgnoreCase( order.status ) ) && ( sumaPagos.compareTo( montoCentavos ) <= 0 )} )
    }
    dealsModel.fireTableDataChanged()
    itemsModel.fireTableDataChanged()
    paymentsModel.fireTableDataChanged()
      if((order?.total - order?.paid) == 0){
          ppButton?.setText('Imprimir')

      } else{
          ppButton?.setText('Pagar')
      }



  }

  private def dateConverter = { Date val ->
    val?.format( 'dd-MM-yyyy' )
  }

  private def currencyConverter = {
    NumberFormat.getCurrencyInstance( Locale.US ).format( it ?: 0 )
  }

  private def doCancel = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    if ( 'T'.equalsIgnoreCase( order.status ) ) {
      sb.optionPane( message: "La venta ya ha sido cancelada, estado: ${order?.status}", optionType: JOptionPane.DEFAULT_OPTION )
          .createDialog( this, "No se puede cancelar" )
          .show()
    } else {
      new CancellationDialog( this, order.id ).show()
      CancellationController.refreshOrder( order )
      doBindings()
    }
    source.enabled = true
  }

  private def doRefund = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    boolean authorized
    if ( AccessController.authorizerInSession ) {
      authorized = true
    } else {
      AuthorizationDialog authDialog = new AuthorizationDialog( this, "Cancelaci\u00f3n requiere autorizaci\u00f3n" )
      authDialog.show()
      authorized = authDialog.authorized
    }
    CancellationController.resetValuesofCancellation( order.id )
    if ( authorized ) {
      new RefundDialog( this, order.id ).show()
      CancellationController.refreshOrder( order )
      doBindings()
    }
    source.enabled = true
  }

  private def doPrintRefund = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    CancellationController.resetValuesofCancellation( order.id )
    CancellationController.printOrderCancellation( order.id )
    printCancellationNotToday( order )
    source.enabled = true
  }

  private  def doSwitchPP = { ActionEvent ev ->
      JButton source = ev.source as JButton
      source.enabled = false


      if(ppButton.getText().equals('Pagar')){
          doShowPayment()
      } else{
          doPrint()
      }

      source.enabled = true
  }

  private doPrint(){
    OrderController.printOrder( order.id, false )
  }

    private  doShowPayment(){
        pagoN=null
        println('Order ID: ' + order?.id)
        String OrderID = order?.id
        if((order?.total - order?.paid) > 0){

            updatePagos()
            PaymentDialog paymentDialog = new PaymentDialog( pago, order, null,this )
            paymentDialog.setVisible(true)
            pagoN = paymentDialog.pagoN

            if(pagoN != null){
                updatePagos()

                this.order.payments.add(Payment.toPaymment(pagoN))
                paymentsModel.fireTableDataChanged()
                this.order?.paid =  this.order?.paid + pagoN?.monto
                this.order?.due = this.order?.due - pagoN?.monto
                doBindings()

                if(pagoN.confirmado == true){
                    println('Order ID: ' + order?.id)
                    OrderController.printPaid(order?.id, pagoN?.id)
                }
            }
        }

        if((order?.total - order?.paid) == 0){
            updatePagos()
            Jb trabajo = OrderController.entraJb(order?.bill)
            if( trabajo == null ){
              String bill = order?.bill.replaceFirst("^0*", "")
              trabajo = OrderController.entraJb(bill)
            }
            if(trabajo != null){
                if(trabajo?.estado.trim().equals('RS')){
                        OrderController.insertaEntrega(order,false)
                        //JOptionPane.showMessageDialog(null,"datos guardados correctamente")

                } else{
                     Integer entregar = JOptionPane.showConfirmDialog(null,"¿Desea entregar trabajo?", "entrega", JOptionPane.YES_NO_OPTION)

                     if(entregar == 0){
                    OrderController.insertaEntrega(order,false)
                    //JOptionPane.showMessageDialog(null,"datos guardados correctamente")
                     }
                }

            }
            ppButton?.setText('Imprimir')
        }

       // updateOrder( order?.id )



    }

    private void updatePagos(){
        String parcialidad = '0'
            List<Pago> pagos =  OrderController.findPagos(order?.id)
            Iterator iterator = pagos.iterator();
            while (iterator.hasNext()) {
                Pago pago = iterator.next()
                if(pago?.idRecibo.equals('')){
                    pago?.idRecibo = OrderController.reciboSeq()
                }
                if(pago?.parcialidad.equals('')){
                    pago?.parcialidad = ((pago?.parcialidad + parcialidad).toInteger() + 1).toString()
                }
                parcialidad = pago?.parcialidad
                OrderController.savePago(pago)
            }

        }

    private void updateOrder( String pOrderId ) {
        //Order tmp = OrderController.getOrder( pOrderId )
       // if ( tmp?.id ) {
        //    order = tmp
           navigatorPanel = new JPanel()
           navigatorPanel.add( new OrderNavigatorPanel( order, {doBindings()} ) )



       // }
    }



    private def printCancellationNotToday(Order orderCom){
        Item item = new Item()
        for(OrderItem i : orderCom.items){
            if(i.item.type.trim().equalsIgnoreCase(GENERICO_ARMAZON)){
                item = i.item
            }
        }
        if(item.id != null){
            CancellationController.printMaterialReturn( order.id )
            CancellationController.printMaterialReception( order.id )
        }
    }


    public void cleanAll( ){
      sb.finalize()
      sb = null
      order.finalize()
      order = null
      navigatorPanel.finalize()
      navigatorPanel = null
    }
}

