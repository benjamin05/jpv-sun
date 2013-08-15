package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.model.Parametro
import mx.lux.pos.model.TipoParametro
import mx.lux.pos.ui.controller.ItemController
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.model.Order
import mx.lux.pos.ui.model.OrderItem
import mx.lux.pos.ui.view.panel.OrderPanel
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.util.List

class ItemDialog extends JDialog {

  private SwingBuilder sb
  private OrderItem tmpOrderItem
  private OrderItem orderItem
  private Order order
  private List<Item> items
  private List<String> colors
  private JSpinner quantity
  private OrderPanel op
  private JComboBox surte
    private List<String> surteOption
    private Boolean surteVisible

    ItemDialog( Component parent, Order order, final OrderItem orderItem, Component orderP) {

        op = orderP
    this.orderItem = orderItem
    this.order = order
    sb = new SwingBuilder()
    tmpOrderItem = new OrderItem(
        item: orderItem?.item,
        quantity: orderItem?.quantity,
        //delivers: orderItem?.delivers
    )
    items = [ ]
    colors = [ ]


        surteVisible = OrderController.surteEnabled(orderItem?.tipo.trim())
        if(surteVisible == true){
            surteOption = OrderController.surteOption(orderItem?.tipo.trim())
        }

        items.addAll( ItemController.findItems( tmpOrderItem.item?.name ) )
    colors.addAll( items*.color )
    buildUI( parent )
    doBindings()
  }

  private void buildUI( Component parent ) {
    sb.dialog( this,
        title: "Artículo ${tmpOrderItem.item?.name ?: ''}",
        location: parent.locationOnScreen,
        resizable: false,
        modal: true,
        pack: true,
        layout: new MigLayout( 'wrap 6', '[fill,grow]20[fill]20[fill]20[fill,grow]' )
    ) {
      label()
        label( )
      label( 'Artículo' )
      label( 'Cantidad' )
      label('Surte', visible: surteVisible )
      label()


        label()
        label()
      label( tmpOrderItem.item?.name )
      quantity = spinner( model: spinnerNumberModel( minimum: 1, stepSize: 1, value: tmpOrderItem.quantity ) )
        surte = comboBox( items: surteOption, visible: surteVisible )
        label()


      panel( layout: new MigLayout( 'right', '[fill,100!]' ), constraints: 'span' ) {
        button( 'Borrar', actionPerformed: doDelete )
        button( 'Aplicar', actionPerformed: doSubmit )
        button( 'Cancelar', defaultButton: true, actionPerformed: {dispose()} )
      }
    }
  }

  private void doBindings( ) {
    sb.build {
      bean( quantity, value: bind( source: tmpOrderItem, sourceProperty: 'quantity', mutual: true ) )
      //bean( color, selectedItem: bind( source: tmpOrderItem.item, sourceProperty: 'color' ) )
      //bean( delivers, selectedItem: bind( source: tmpOrderItem, sourceProperty: 'delivers', mutual: true ) )
    }
  }

  private def colorChanged = { ItemEvent ev ->
    if ( ev.stateChange == ItemEvent.SELECTED ) {
      tmpOrderItem.item = items.find {
        it?.color?.equalsIgnoreCase( ev.item as String )
      }
    } else {
      tmpOrderItem.item = null
    }
  }

  private def doDelete = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false

    Order o = OrderController.removeOrderItemFromOrder( order.id, orderItem )
      if(orderItem?.tipo.trim().equals('A')){
          op.armazonString = null
      }

    source.enabled = true
    this.setVisible(false)
    //dispose()
  }

  private def doSubmit = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    OrderController.surteCallWS(order,tmpOrderItem?.item)
    OrderController.removeOrderItemFromOrder( order.id, orderItem )
    OrderController.addOrderItemToOrder( order.id, tmpOrderItem )
    source.enabled = true
    dispose()
  }
}
