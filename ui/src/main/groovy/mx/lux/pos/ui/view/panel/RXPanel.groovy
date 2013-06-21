package mx.lux.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.Branch
import mx.lux.pos.ui.model.Session
import mx.lux.pos.ui.model.SessionItem
import mx.lux.pos.ui.view.panel.RXPanel
import mx.lux.pos.ui.model.Rx
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.view.dialog.EditRxDialog
import mx.lux.pos.ui.view.renderer.DateCellRenderer
import net.miginfocom.swing.MigLayout

import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseEvent

/**
 * Created with IntelliJ IDEA.
 * User: OmarCortes
 * Date: 3/06/13
 * Time: 09:14 AM
 * To change this template use File | Settings | File Templates.
 */
class RXPanel extends JPanel {


    private static final String TXT_TAB_TITLE = 'Rx'

    private SwingBuilder sb

    private JTextField txtEmpleado
    private JLabel lblEmpleado
    private JLabel lblFolio
    private ButtonGroup groupUso
    private JRadioButton rbLejos
    private JRadioButton rbCerca
    private JRadioButton rbBifocal
    private JRadioButton rbProgresivo
    private JRadioButton rbIntermedio
    private JRadioButton rbBifocalInter

    private JTextField txtOdEsfera
    private JTextField txtOdCil
    private JTextField txtOdEje
    private JTextField txtOdAd
    private JTextField txtOdAv
    private JTextField txtOdDm
    private JTextField txtOdPrisma
    private JTextField txtOdUbic

    private JTextField txtOiEsfera
    private JTextField txtOiCil
    private JTextField txtOiEje
    private JTextField txtOiAd
    private JTextField txtOiAv
    private JTextField txtOiDm
    private JTextField txtOiPrisma
    private JTextField txtOiUbic

    private JTextField txtDICerca
    private JTextField txtAltOblea
    private JTextField txtDILejos

    private JTextField txtObservaciones

    private JPanel empleadoPanel
    private JPanel usoRxPanel1
    private JPanel usoRxPanel2

    private DefaultTableModel rxModel
    private List<Rx> lstRecetas
    private Rx receta
    private Integer idCliente


    private final String EDITAR = 'Editar Receta'
    private final String NUEVA = 'Nueva Receta'

    public boolean cancel = false

    public RXPanel( Integer idCliente )
    {
        sb = new SwingBuilder()
        lstRecetas = [ ] as ObservableList
        lstRecetas.addAll( CustomerController.findAllPrescriptions( idCliente ) )
        this.idCliente = idCliente
        buildUI()
    }

    private void buildUI( ) {
        sb.panel( this, border: BorderFactory.createEmptyBorder( 10, 5, 10, 5 ) ) {
            borderLayout()
            scrollPane( constraints: BorderLayout.CENTER, mousePressed: doNewRx ) {
                table( selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doClick, mousePressed: doPress ) {
                    rxModel = tableModel( list: lstRecetas ) {
                        closureColumn( header: 'Fecha', read: {Rx tmp -> tmp?.rxDate}, minWidth: 90, cellRenderer: new DateCellRenderer() )
                        closureColumn( header: 'Optometrista', read: {Rx tmp -> tmp?.optometristName}, minWidth: 190 )
                        closureColumn( header: 'Tipo', read: {Rx tmp -> tmp?.getTipo()}, minWidth: 90 )
                        closureColumn( header: 'Folio', read: {Rx tmp -> tmp?.folio} )
                    } as DefaultTableModel
                }
            }
            detailsRx( BorderLayout.PAGE_END )
        }

    }

    private JPanel detailsRx( String pConstraint ) {
        JPanel details = sb.panel( constraints: pConstraint ) {
            borderLayout()
            panel( minimumSize: [ 660, 150 ] as Dimension,
                    border: titledBorder( "Rx" ),
                    constraints: BorderLayout.CENTER,
                    layout: new MigLayout( 'fill,wrap 11',
                            '[][fill,grow][fill,grow][fill,grow][fill,grow][fill,grow][fill,grow][fill,grow][fill,grow]30[fill][fill,grow]' )
            ) {
                label()
                label( text: 'Esfera', horizontalAlignment: JTextField.CENTER )
                label( text: 'Cil.', toolTipText: 'Cilindro', horizontalAlignment: JTextField.CENTER )
                label( text: 'Eje', horizontalAlignment: JTextField.CENTER )
                label( text: 'Ad.', toolTipText: 'Adición', horizontalAlignment: JTextField.CENTER )
                label( text: 'A.V.', toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.CENTER )
                label( text: 'D.M.', toolTipText: 'Distancia Monocular', horizontalAlignment: JTextField.CENTER )
                label( text: 'Prisma', horizontalAlignment: JTextField.CENTER )
                label( text: 'Ubic.', toolTipText: 'Ubicación', horizontalAlignment: JTextField.CENTER )
                label( text: 'D.I. Lejos', toolTipText: 'Distancia Interpupilar Lejos' )
                txtDILejos = textField(
                        editable: false,
                        minimumSize: [ 20, 20 ] as Dimension,
                        toolTipText: 'Distancia Interpupilar Lejos',
                        horizontalAlignment: JTextField.RIGHT
                )

                label( text: 'O.D.', toolTipText: 'Ojo Derecho' )
                txtOdEsfera = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOdCil = textField( editable: false, toolTipText: 'Cilindro', horizontalAlignment: JTextField.RIGHT )
                txtOdEje = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOdAd = textField( editable: false, toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT )
                txtOdAv = textField( editable: false, toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT )
                txtOdDm = textField(
                        editable: false,
                        toolTipText: 'Distancia Monocular',
                        horizontalAlignment: JTextField.RIGHT
                )
                txtOdPrisma = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOdUbic = textField( editable: false, toolTipText: 'Ubicación', horizontalAlignment: JTextField.LEFT )
                label( text: 'D.I. Cerca', toolTipText: 'Distancia Interpupilar Cerca' )
                txtDICerca = textField(
                        editable: false,
                        minimumSize: [ 20, 20 ] as Dimension,
                        toolTipText: 'Distancia Interpupilar Cerca',
                        horizontalAlignment: JTextField.RIGHT
                )

                label( text: 'O.I.', toolTipText: 'Ojo Izquierdo' )
                txtOiEsfera = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOiCil = textField( editable: false, toolTipText: 'Cilindro', horizontalAlignment: JTextField.RIGHT )
                txtOiEje = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOiAd = textField( editable: false, toolTipText: 'Adición', horizontalAlignment: JTextField.RIGHT )
                txtOiAv = textField( editable: false, toolTipText: 'Agudeza Visual', horizontalAlignment: JTextField.LEFT )
                txtOiDm = textField(
                        editable: false,
                        toolTipText: 'Distancia Monocular',
                        horizontalAlignment: JTextField.RIGHT
                )
                txtOiPrisma = textField( editable: false, horizontalAlignment: JTextField.RIGHT )
                txtOiUbic = textField( editable: false, toolTipText: 'Ubicación', horizontalAlignment: JTextField.LEFT )
                label( text: 'Alt. Oblea', toolTipText: 'Altura Oblea' )
                txtAltOblea = textField(
                        editable: false,
                        minimumSize: [ 20, 20 ] as Dimension,
                        toolTipText: 'Altura Oblea',
                        horizontalAlignment: JTextField.RIGHT
                )
            }
            panel( constraints: BorderLayout.PAGE_END, border: BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) ) {
                borderLayout()
                label( text: 'Observaciones:', constraints: BorderLayout.LINE_START )
                txtObservaciones = textField( editable: false, constraints: BorderLayout.CENTER )
            }

        }
        return details
    }

    private void doBindings( ) {
        sb.build {
            bean( txtOdEsfera, text: bind( source: receta, sourceProperty: 'odEsfR' ) )
            bean( txtOdCil, text: bind( source: receta, sourceProperty: 'odCilR' ) )
            bean( txtOdEje, text: bind( source: receta, sourceProperty: 'odEjeR' ) )
            bean( txtOdAd, text: bind( source: receta, sourceProperty: 'odAdcR' ) )
            if ( receta?.odAvR != null ) {
                bean( txtOdAv, text: bind( source: receta, sourceProperty: 'odAvR' ) )
            } else {
                txtOdAv.setText( '20/' )
            }
            bean( txtOdDm, text: bind( source: receta, sourceProperty: 'diOd' ) )
            bean( txtOdPrisma, text: bind( source: receta, sourceProperty: 'odPrismH' ) )
            bean( txtOdUbic, text: bind( source: receta, sourceProperty: 'odPrismaV' ) )
            bean( txtDILejos, text: bind( source: receta, sourceProperty: 'diLejosR' ) )
            bean( txtOiEsfera, text: bind( source: receta, sourceProperty: 'oiEsfR' ) )
            bean( txtOiCil, text: bind( source: receta, sourceProperty: 'oiCilR' ) )
            bean( txtOiEje, text: bind( source: receta, sourceProperty: 'oiEjeR' ) )
            bean( txtOiAd, text: bind( source: receta, sourceProperty: 'oiAdcR' ) )
            if ( receta?.oiAvR != null ) {
                bean( txtOiAv, text: bind( source: receta, sourceProperty: 'oiAvR' ) )
            } else {
                txtOiAv.setText( '20/' )
            }
            bean( txtOiDm, text: bind( source: receta, sourceProperty: 'diOi' ) )
            bean( txtOiPrisma, text: bind( source: receta, sourceProperty: 'oiPrismH' ) )
            bean( txtOiUbic, text: bind( source: receta, sourceProperty: 'oiPrismaV' ) )
            bean( txtDICerca, text: bind( source: receta, sourceProperty: 'diCercaR' ) )
            bean( txtAltOblea, text: bind( source: receta, sourceProperty: 'altOblR' ) )
            bean( txtObservaciones, text: bind( source: receta, sourceProperty: 'observacionesR' ) )
        }
    }

    private def doClick = { MouseEvent ev ->
        if ( SwingUtilities.isLeftMouseButton( ev ) && ev.source.selectedElement != null ) {
            Rx selection = ev.source.selectedElement as Rx
            receta = selection
            println "Receta ${receta.id} seleccionada"
            doBindings()
        }
    }

    private def doPress = { MouseEvent ev ->
        if ( SwingUtilities.isRightMouseButton( ev ) && ev.source.selectedElement != null ) {
            Rx selection = ev.source.selectedElement as Rx
            if ( selection.id ) {
                sb.popupMenu {
                    menuItem( text: 'Editar',
                            actionPerformed: {
                                EditRxDialog editRx = new EditRxDialog( this, selection, selection.idClient, selection.idStore, EDITAR )
                                editRx.show()
                                lstRecetas.clear()
                                lstRecetas.addAll( CustomerController.findAllPrescriptions( idCliente ) )
                                doBindings()
                                rxModel.fireTableDataChanged()
                            }
                    )
                    menuItem( text: 'Nueva Receta',
                            actionPerformed: {
                                EditRxDialog editRx = new EditRxDialog( this, new Rx(), selection.idClient, selection.idStore, NUEVA )
                                editRx.show()
                                lstRecetas.clear()
                                lstRecetas.addAll( CustomerController.findAllPrescriptions( idCliente ) )
                                doBindings()
                                rxModel.fireTableDataChanged()
                            }
                    )
                }.show( ev.component, ev.x, ev.y )
            }
        }
    }

    private def doNewRx = { MouseEvent ev ->

        if ( SwingUtilities.isRightMouseButton( ev ) ) {
            sb.popupMenu {
                menuItem( text: 'Nueva Receta',
                        actionPerformed: {
                             //Para pruebas con el popup
                            Branch branch = Session.get( SessionItem.BRANCH ) as Branch
                            idCliente = 1
                            //Para pruebas con el popup

                            EditRxDialog editRx = new EditRxDialog( this, new Rx(), idCliente, branch?.id, NUEVA )
                            editRx.show()
                            lstRecetas.clear()
                            lstRecetas.addAll( CustomerController.findAllPrescriptions( idCliente ) )
                            doBindings()
                            rxModel.fireTableDataChanged()
                        }
                )
            }.show( ev.component, ev.x, ev.y )
        }
    }


    private void edit( ) {
        empleadoPanel.visible = true
        usoRxPanel1.visible = true
        usoRxPanel2.visible = true
        txtOdEsfera.editable = true
        txtOdCil.editable = true
        txtOdEje.editable = true
        txtOdAd.editable = true
        txtOdAv.editable = true
        txtOdDm.editable = true
        txtOdPrisma.editable = true
        txtOdUbic.editable = true
        txtOiEsfera.editable = true
        txtOiCil.editable = true
        txtOiEje.editable = true
        txtOiAd.editable = true
        txtOiAv.editable = true
        txtOiDm.editable = true
        txtOiPrisma.editable = true
        txtOiUbic.editable = true
        txtDICerca.editable = true
        txtAltOblea.editable = true
        txtDILejos.editable = true
        txtObservaciones.editable = true
    }

    String getTitle( ) {
        return TXT_TAB_TITLE
    }

}
