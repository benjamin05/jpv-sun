package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.FormaContacto
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.ui.controller.ContactController
import mx.lux.pos.ui.resources.UI_Standards
import net.miginfocom.swing.MigLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.*
import java.awt.*
import java.util.List

class ContactClientDialog extends JDialog {



  private SwingBuilder sb = new SwingBuilder()
  private Logger logger = LoggerFactory.getLogger( this.getClass() )

  private List<FormaContacto> formasContacto
  private FormaContacto selection
  private NotaVenta notaVenta
  private JTable tFormas
  private DefaultTableModel model


    ContactClientDialog( NotaVenta notaVenta) {
    this.notaVenta = notaVenta
    this.formasContacto = ContactController.findByIdCliente(notaVenta?.idCliente)
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


        scrollPane( constraints: BorderLayout.CENTER) {
          tFormas = table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
            model = tableModel( list: formasContacto ) {
                closureColumn(header:'Tipo de Contacto',minWidth: 180, read:{row -> return row.tipoContacto.descripcion})
                closureColumn(header:'Dato',minWidth: 180, read:{row -> return row.contacto})
            } as DefaultTableModel
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
      this.selection = null
    this.setVisible( false )
  }

  protected void onSelection( ) {

      int index = tFormas.convertRowIndexToModel(tFormas.getSelectedRow())
      if (tFormas.selectedRowCount > 0) {
          this.logger.debug( String.format('Selected Row:%d', index) )
          selection = this.formasContacto.getAt( index)
          this.setVisible( false )
      } else {
          this.logger.debug( 'No Row Selected' )
          sb.doLater {
              this.onCancel()
          }
      }

  }


    void activate( ) {

        this.selection = null

        this.setVisible( true )
    }

    FormaContacto getFormaContactoSeleted( ) {
        return selection
    }


    protected void addContact(){
        ContactDialog contacto = new ContactDialog(notaVenta)
        contacto.activate()
        onCancel()
    }


}

