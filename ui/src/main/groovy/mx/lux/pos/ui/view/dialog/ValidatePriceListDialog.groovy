package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.PriceListController
import mx.lux.pos.ui.model.PriceList
import net.miginfocom.swing.MigLayout

import java.awt.Component

class ValidatePriceListDialog {

  private def sb = new SwingBuilder()
  private def items = [ ]

  ValidatePriceListDialog( Component component, PriceList priceList ) {
    items = PriceListController.validatePriceList( priceList )

    sb.dialog( title: "Validar Lista de Precios: ${priceList?.id}",
        locationRelativeTo: component,
        resizable: false,
        modal: true,
        pack: true,
        show: true,
        layout: new MigLayout( 'wrap', '[fill]' )
    ) {
      label( "Ubicación de Artículos" )

      scrollPane {
        table() {
          tableModel( list: items ) {
            closureColumn( header: 'Artículo', read: {it?.name} )
            closureColumn( header: 'Color', read: {it?.color} )
            closureColumn( header: 'Ubicaciones', read: {it?.location} )
          }
        }
      }

      panel( layout: new MigLayout( 'fill', '[right]' ) ) {
        button( 'Imprimir',
            actionPerformed: { ev ->
              ev.source.enabled = false
              PriceListController.printPriceListLocation( priceList )
              ev.source.enabled = true
            }
        )
        button( 'Cerrar', defaultButton: true, actionPerformed: {dispose()} )
      }
    }
  }
}
