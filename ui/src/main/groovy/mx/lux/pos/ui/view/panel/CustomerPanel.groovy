package mx.lux.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.FormaContacto
import mx.lux.pos.ui.controller.ContactController
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.dialog.ContactDialogNewCustomer
import mx.lux.pos.ui.view.dialog.NewCustomerAndRxDialog
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils

import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent

import mx.lux.pos.ui.model.*

import javax.swing.*
import java.awt.event.MouseEvent

class CustomerPanel extends JPanel {

    private static final String TXT_TAB_TITLE = 'Cliente'

    private SwingBuilder sb
    private Customer customer = new Customer()
    private String defaultState
    private List<String> states
    private List<String> domains
    private List<LinkedHashMap<String, String>> locations
    private List<LinkedHashMap<String, Object>> titles
    private Contact tmpHomeContact
    private Contact tmpEmailContact
    private JTextField firstName
    private JTextField fathersName
    private JTextField mothersName
    private JTextField primary
    private JTextField homePhone
    private JTextField email
    private JComboBox salutation
    private JComboBox stateField
    private JComboBox locationField
    private JComboBox gender
    private JComboBox city
    private JComboBox zipcode
    private JComboBox domain
    private JSpinner dob
    private JPanel customerPanel
    private boolean edit
    public boolean cancel = false
    private NewCustomerAndRxDialog CustomerAndDialog = null

    private static JComboBox tipo
    private static List<String> tipos
    private static JComboBox dominio
    private static JLabel arroba
    private static List<String> dominios
    private static JTextField infoTipo
    private static JTextField correo

    private static JPanel contactEdit
    private static JPanel contactReg
    private static JPanel addContact
    private List<FormaContacto> formasContacto = new ArrayList<FormaContacto>()


    private JTable tFormas
    private DefaultTableModel model

    CustomerPanel(Component parent, Customer customer, boolean editar) {
        CustomerAndDialog = parent
        edit = editar

        if (editar == true) {
            this.formasContacto = ContactController.findCustomerContact(customer?.id)

        }
        sb = new SwingBuilder()
        this.customer = customer
        defaultState = CustomerController.findDefaultState()
        states = CustomerController.findAllStates()
        titles = CustomerController.findAllCustomersTitles()
        //domains = CustomerController.findAllCustomersDomains()
        tipos = CustomerController.findAllContactTypes()
        dominios = CustomerController.findAllCustomersDomains()
        locations = []
        tmpHomeContact = new Contact(type: ContactType.HOME_PHONE)
        tmpEmailContact = new Contact(type: ContactType.EMAIL)
        initialize(customer)
        buildUI()
        doBindings()
    }

    Customer getCustomer() {
        return customer
    }

    private void initialize(Customer customer) {

        if (edit == true) {
            this.customer.type = customer?.type
            this.customer.rfc = customer.rfc
            this.customer.gender = customer.gender
            this.customer.address = customer.address
        } else {
            this.customer.type = CustomerType.DOMESTIC
            this.customer.rfc = CustomerType.DOMESTIC.rfc
            this.customer.gender = GenderType.MALE
            this.customer.address = new Address(state: defaultState)
        }
        //  this.customer.contacts = [ ]
        if (customer?.id) {
            this.customer.id = customer.id
            this.customer.name = customer.name
            this.customer.fathersName = customer.fathersName
            this.customer.mothersName = customer.mothersName
            this.customer.title = customer.title
            this.customer.legalEntity = customer.legalEntity
            this.customer.rfc = customer.rfc
            this.customer.dob = customer.dob
            this.customer.gender = customer.gender
            if (customer.address) {
                this.customer.address = new Address(
                        primary: customer.address.primary,
                        zipcode: customer.address.zipcode,
                        location: customer.address.location,
                        city: customer.address.city,
                        state: customer.address.state
                )
            }
            /* if ( customer.contacts?.any() ) {
                 customer.contacts.each { Contact tmp ->
                     this.customer.contacts.add( tmp )
                     switch ( tmp.type ) {
                         case ContactType.HOME_PHONE:
                             tmpHomeContact = tmp
                             break
                         case ContactType.EMAIL:
                             tmpEmailContact = tmp
                             List<String> emailTokens = tmp.primary?.tokenize( '@' )
                             tmpEmailContact.primary = emailTokens?.first()
                             tmpEmailContact.extra = emailTokens?.last()
                             break
                     }
                 }
             }  */
        }
    }

    private void buildUI() {
        sb.panel(this, layout: new MigLayout('fill,wrap', '[fill]')) {
            panel(border: titledBorder(''), layout: new MigLayout('wrap 4', '[][fill,grow][][fill,grow]')) {
                label('Saludo')
                salutation = comboBox(items: titles*.title, itemStateChanged: titleChanged)

                label('Sexo')
                gender = comboBox(items: GenderType.values())

                label('Nombre')
                firstName = textField(document: new UpperCaseDocument()/*, inputVerifier: new NotEmptyVerifier()*/)

<<<<<<< HEAD
                label( 'Apellido Paterno' )
                fathersName = textField( document: new UpperCaseDocument()/*, inputVerifier: new NotEmptyVerifier()*/ )

                label( 'Apellido Materno' )
                mothersName = textField( document: new UpperCaseDocument() )

                label( 'F. Nacimiento' )
                dob = spinner( model: spinnerDateModel() )

=======
                label('F. Nacimiento')
                dob = spinner(model: spinnerDateModel())

                label('Apellido Paterno')
                fathersName = textField(document: new UpperCaseDocument()/*, inputVerifier: new NotEmptyVerifier()*/)

                label('Apellido Materno')
                mothersName = textField(document: new UpperCaseDocument())
>>>>>>> origin/MasVision
            }

            panel(border: titledBorder('Dirección'), layout: new MigLayout('wrap 3', '[][fill,grow][]')) {
                label('Calle y Número')
                primary = textField(document: new UpperCaseDocument()/*, inputVerifier: new NotEmptyVerifier()*/, constraints: 'span 2')

                label('Estado')
                stateField = comboBox(items: states, itemStateChanged: stateChanged, constraints: 'span 2')

                label('Delegación/Mnpo')
                city = comboBox(itemStateChanged: cityChanged, constraints: 'span 2')

                label('Colonia')
                locationField = comboBox(itemStateChanged: locationChanged, constraints: 'span 2')

                label('C.P.')
                zipcode = comboBox(editable: true)
                button('Buscar', actionPerformed: doSearch)
            }
            /*
  panel( border: titledBorder( 'Contacto' ), layout: new MigLayout( '', '[][fill,180!][center,25!][fill,180!]' ) ) {
      label( text: ContactType.HOME_PHONE )
      homePhone = textField( constraints: 'wrap' )

      label( text: ContactType.EMAIL )
      email = textField()
      label( '@' )
      domain = comboBox( editable: true, items: domains )
  }
              */

            panel(border: titledBorder('Contacto'), layout: new MigLayout("wrap", "[fill,grow]")) {

                contactReg = panel(layout: new MigLayout("wrap", "[fill,grow]"), visible: true, constraints: 'span 2') {
                    scrollPane(
                            mouseClicked: { MouseEvent ev -> onMouseClickedAtContact(ev) },
                            mouseReleased: { MouseEvent ev -> onMouseClickedAtContact(ev) }) {
                        tFormas = table(selectionMode: ListSelectionModel.SINGLE_SELECTION,
                                mouseClicked: { MouseEvent ev -> onMouseClickedAtContact(ev) },
                                mouseReleased: { MouseEvent ev -> onMouseClickedAtContact(ev) }) {
                            model = tableModel(list: formasContacto) {
                                closureColumn(header: 'Tipo de Contacto', minWidth: 250, read: { row -> return row.tipoContacto.descripcion })
                                closureColumn(header: 'Dato', minWidth: 350, read: { row -> return row.contacto })
                            } as DefaultTableModel
                        }
                    }
                }


            }








            panel(layout: new MigLayout('right', '[fill,100!]')) {
                button('Borrar',
                        visible: customer?.id ? true : false,
                        actionPerformed: doDelete,
                        preferredSize: UI_Standards.BUTTON_SIZE
                )
                button('Aplicar',
                        actionPerformed: doSubmit,
                        preferredSize: UI_Standards.BUTTON_SIZE

                )
                button('Limpiar',
                        visible: customer?.id ? false : true,
                        actionPerformed: doClear,
                        constraints: 'hidemode 3',
                        preferredSize: UI_Standards.BUTTON_SIZE
                )
            }
        }

        dob.editor = new JSpinner.DateEditor(dob as JSpinner, 'dd-MM-yyyy')
    }



    private void doBindings() {
        sb.build {
            bean(firstName, text: bind(source: customer, sourceProperty: 'name', mutual: true))
            bean(fathersName, text: bind(source: customer, sourceProperty: 'fathersName', mutual: true))
            bean(mothersName, text: bind(source: customer, sourceProperty: 'mothersName', mutual: true))
            bean(salutation, selectedItem: bind(source: customer, sourceProperty: 'title', mutual: true))
            bean(dob, value: bind(source: customer, sourceProperty: 'dob', mutual: true))
            bean(gender, selectedItem: bind(source: customer, sourceProperty: 'gender', mutual: true))
            bean(primary, text: bind(source: customer.address, sourceProperty: 'primary', mutual: true))
            bean(stateField, selectedItem: bind(source: customer.address, sourceProperty: 'state', mutual: true))
            bean(city, selectedItem: bind(source: customer.address, sourceProperty: 'city', mutual: true))
            bean(locationField, selectedItem: bind(source: customer.address, sourceProperty: 'location', mutual: true))
            bean(zipcode, selectedItem: bind(source: customer.address, sourceProperty: 'zipcode', mutual: true))
            // bean( homePhone, text: bind( source: tmpHomeContact, sourceProperty: 'primary', mutual: true ) )
            // bean( email, text: bind( source: tmpEmailContact, sourceProperty: 'primary', mutual: true ) )
            // bean( domain, selectedItem: bind( source: tmpEmailContact, sourceProperty: 'extra', mutual: true ) )
        }
    }

    private def doSearch = { ActionEvent ev ->
        JButton source = ev.source as JButton
        source.enabled = false
        List<Address> results = CustomerController.findAddresesByZipcode(zipcode.selectedItem as String) ?: []
        if (results.any()) {
            JOptionPane inputPane = sb.optionPane(message: 'Selecciona una colonia',
                    selectionValues: results*.location,
                    optionType: JOptionPane.OK_CANCEL_OPTION
            )
            inputPane.createDialog(zipcode, 'Resultados de búsqueda por C.P.').show()
            String selection = inputPane?.inputValue as String
            Address tmpAddress = results.find { Address tmp ->
                tmp?.location?.equalsIgnoreCase(selection)
            }
            if (tmpAddress != null) {
                sb.doOutside {
                    stateField.selectedItem = tmpAddress.state
                    city.selectedItem = tmpAddress.city
                    locationField.selectedItem = tmpAddress.location
                }
            }
        } else {
            sb.optionPane(message: 'No se encontraron resultados')
                    .createDialog(zipcode, 'No se encontraron resultados')
                    .show()
        }
        source.enabled = true
    }

    private def doDelete = { ActionEvent ev ->
        JButton source = ev.source as JButton
        source.enabled = false
        //dispose()
    }

    private boolean isValidInput() {
        if (StringUtils.isNotBlank(firstName.text)) {
            return true
        } else {
            sb.optionPane(
                    message: 'Se debe registrar el nombre',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog(this, 'No se puede registrar la venta')
                    .show()
        }
        return false
    }



    private def doSubmit = { ActionEvent ev ->
        JButton source = ev.source as JButton
        source.enabled = false
        customer?.contacts?.clear()
        /*
        if (StringUtils.isNotBlank(tmpHomeContact?.primary)) {
            customer.contacts.add(tmpHomeContact)
        }
        if (StringUtils.isNotBlank(tmpEmailContact?.primary)) {
            String mail = "${tmpEmailContact.primary}@${tmpEmailContact.extra}"
            tmpEmailContact.primary = mail
            customer.contacts.add(tmpEmailContact)
        }
        */


        if (isValidInput()) {
            Customer tmpCustomer = new Customer()
            if (edit == false) {
                tmpCustomer = CustomerController.addCustomer(this.customer)

                CustomerController.addClienteProceso(tmpCustomer)        //Se agrega registro en la tabla cliente_proceso
            } else {
                tmpCustomer = customer
            }
            if (tmpCustomer?.id) {

                customer = tmpCustomer
                CustomerController.saveContact(customer,0,'')
                for(int b=1;b<=3;b++){
                CustomerController.saveContact(customer,b,'')
                }
                for (int a = 0; a < this.formasContacto.size(); a++) {

                    if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 1) {
                         CustomerController.saveContact(customer,0,this.formasContacto.getAt(a)?.contacto)
                    } else if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 2) {
                        CustomerController.saveContact(customer,1,this.formasContacto.getAt(a)?.contacto)
                    } else if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 3) {
                        CustomerController.saveContact(customer,2,this.formasContacto.getAt(a)?.contacto)
                    } else  if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 4) {
                        CustomerController.saveContact(customer,3,this.formasContacto.getAt(a)?.contacto)
                    }
                }

                this.doCancel()

                //Agregar Recetas
                /*    Integer agregaReceta = JOptionPane.showConfirmDialog(null,"Agregar receta", "¿Desea agregar una receta?", JOptionPane.YES_NO_OPTION);
                    if(agregaReceta == 0){
                        Branch branch = Session.get( SessionItem.BRANCH ) as Branch
                        EditRxDialog editRx = new EditRxDialog( this, new Rx(), customer?.id, branch?.id, 'Nueva Receta' )
                        editRx.show()
                           this.doCancel()



                } else{
                    this.doCancel()
                }
                */
            }
            source.enabled = true
        }
    }

    protected void onMouseClickedAtContact(MouseEvent pEvent) {
        List<String> list = ['Correo', 'Recados', 'Telefono', 'SMS']
        if (SwingUtilities.isRightMouseButton(pEvent) && (pEvent.getID() == MouseEvent.MOUSE_CLICKED)) {

            for (int a = 0; a < this.formasContacto.size(); a++) {
                  println('id_tipo_contacto: '+this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto)
                if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 1) {
                   list.remove('Correo')
                } else if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 2) {
                    list.remove('Recados')
                } else if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 3) {
                    list.remove('Telefono')
                } else  if (this.formasContacto.getAt(a)?.tipoContacto?.id_tipo_contacto == 4) {
                    list.remove('SMS')
                }
            }

            if(list.size()>0){

            ContactDialogNewCustomer contacto = new ContactDialogNewCustomer(new FormaContacto(), false, list)
            contacto.activate()

                if (contacto.formaContacto?.tipoContacto?.id_tipo_contacto != null) {
                    this.formasContacto.add(contacto.formaContacto)
                    model.fireTableDataChanged()
                }

            }
        }
        if (SwingUtilities.isLeftMouseButton(pEvent)) {
            if (pEvent.clickCount == 2) {

                int index = tFormas.convertRowIndexToModel(tFormas.getSelectedRow())
                if (tFormas.selectedRowCount > 0) {

                    ContactDialogNewCustomer contacto = new ContactDialogNewCustomer(this.formasContacto.getAt(index), true, list)
                    contacto.activate()
                    if(contacto.borrar){
                        this.formasContacto.remove(index)
                        model.fireTableDataChanged()
                    }else{
                    this.formasContacto.remove(index)
                    this.formasContacto.add(contacto.formaContacto)
                    model.fireTableDataChanged()
                    }
                }

            }
        }
    }

    private void doCancel() {

        CustomerAndDialog.setVisible(false)
        // this.parent.setVisible(false)

        sb.dispose()
    }

    boolean getCancel() {
        return cancel
    }

    private def doClear = {
        firstName.text = null
        fathersName.text = null
        mothersName.text = null
        salutation.selectedItem = null
        dob.value = new Date()
        gender.selectedItem = GenderType.MALE
        primary.text = null
        stateField.selectedItem = defaultState
        //homePhone.text = null
        //email.text = null
        //domain.selectedItem = null
        city.selectedItem = null
        locationField.selectedItem = null
        zipcode.selectedItem = null
    }

    private def titleChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED) {
            String title = ev.item
            def tmpTitle = titles.find {
                it?.title?.equalsIgnoreCase(title)
            }
            switch (tmpTitle?.gender) {
                case 'f':
                    gender.selectedItem = GenderType.FEMALE
                    break
                case 'm':
                    gender.selectedItem = GenderType.MALE
                    break
            }
        }
    }



    private def stateChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED) {
            String stateName = ev.item
            List<String> results = CustomerController.findCitiesByStateName(stateName) ?: []
            results.each {
                city.addItem(it)
            }
            city.selectedIndex = -1
        } else {
            city.removeAllItems()
            locationField.removeAllItems()
            zipcode.removeAllItems()
            locations.clear()
        }
    }

    private def cityChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED) {
            String stateName = stateField.selectedItem
            String cityName = ev.item
            Set<String> zipcodes = []
            locations = CustomerController.findLocationsByStateNameAndCityName(stateName, cityName) ?: []
            locations.each {
                locationField.addItem(it?.location)
                zipcodes.add(it?.zipcode)
            }
            locationField.selectedIndex = -1
            zipcodes.sort().each {
                zipcode.addItem(it)
            }
        } else {
            locationField.removeAllItems()
            zipcode.removeAllItems()
            locations.clear()
        }
    }

    private def locationChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED) {
            String locationName = ev.item
            def result = locations.find {
                it?.location?.equalsIgnoreCase(locationName)
            }
            zipcode.selectedItem = result?.zipcode
        }
    }

    String getTitle() {
        return TXT_TAB_TITLE
    }

    private void addContact() {
        contactReg.visible = true
        contactEdit.visible = true
        addContact.visible = false

    }

    private def typeChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED) {
            String typeName = ev.item

            if (typeName.trim() != 'CORREO') {
                infoTipo.setVisible(true)
                correo.setVisible(false)
                arroba.setVisible(false)
                dominio.setVisible(false)
            } else {
                infoTipo.setVisible(false)
                correo.setVisible(true)
                arroba.setVisible(true)
                dominio.setVisible(true)
            }

        } else {

        }
    }
}
