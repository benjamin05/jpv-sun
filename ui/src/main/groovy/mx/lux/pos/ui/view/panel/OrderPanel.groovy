package mx.lux.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.*
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.MainWindow
import mx.lux.pos.ui.controller.*
import mx.lux.pos.ui.model.*
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.component.DiscountContextMenu
import mx.lux.pos.ui.view.dialog.*
import mx.lux.pos.ui.view.driver.PromotionDriver
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.*
import java.awt.*
import java.awt.event.*
import java.text.NumberFormat
import java.util.List

class OrderPanel extends JPanel
implements IPromotionDrivenPanel, FocusListener, CustomerListener {


    static final String MSG_INPUT_QUOTE_ID = 'Indique el número de cotización'
    static final String TXT_QUOTE_TITLE = 'Seleccionar cotización'
    static final String TXT_INVALID_PAYMENT_TITLE = 'Los pagos no cumplen con la política comercial.'
    private static final String TXT_BTN_CLOSE = 'Vendedor'
    private static final String TXT_BTN_QUOTE = 'Cotizar'
    private static final String TXT_BTN_PRINT = 'Imprimir'
    private static final String TXT_BTN_NEW_ORDER = 'Otra venta'
    private static final String TXT_BTN_CONTINUE = 'Continuar'
    private static final String TXT_NO_ORDER_PRESENT = 'Se debe agregar al menos un artículo.'
    private static final String TXT_PAYMENTS_PRESENT = 'Elimine los pagos registrados y reintente.'
    private static final String MSJ_VENTA_NEGATIVA = 'No se pueden agregar artículos sin existencia.'
    private static final String TXT_VENTA_NEGATIVA_TITULO = 'Error al agregar artículo'
    private static final String TXT_REQUEST_NEW_ORDER = 'Solicita nueva orden a mismo cliente.'
    private static final String TXT_REQUEST_CONTINUE = 'Solicita nueva orden a otro cliente.'
    private static final String TXT_REQUEST_QUOTE = 'Cotizar orden actual.'
    private static final String MSJ_QUITAR_PAGOS = 'Elimine los pagos antes de cerrar la sesion.'
    private static final String TXT_QUITAR_PAGOS = 'Error al cerrar sesion.'
    private static final String MSJ_CAMBIAR_VENDEDOR = 'Esta seguro que desea salir de esta sesion.'
    private static final String TXT_CAMBIAR_VENDEDOR = 'Cerrar Sesion'
    private static final String TAG_GENERICO_B = 'B'

    private Logger logger = LoggerFactory.getLogger(this.getClass())
    private SwingBuilder sb
    private Order order
    private Customer customer
    private JComboBox operationType
    private JButton customerName
    private JButton closeButton
    private JButton quoteButton
    private JButton printButton
    private JButton continueButton
    private JButton newOrderButton
    private JTextArea comments
    private JTextField itemSearch
    private List<IPromotionAvailable> promotionList
    private Collection<OperationType> customerTypes = OperationType.values()
    private DefaultTableModel itemsModel
    private DefaultTableModel paymentsModel
    private DefaultTableModel promotionModel
    private JLabel folio
    private JLabel bill
    private JLabel date
    private JLabel total
    private JLabel paid
    private JLabel due
    private JLabel change

    private DiscountContextMenu discountMenu
    private OperationType currentOperationType
    private Boolean uiEnabled
    private Receta rec
    private Dioptra dioptra
    private Dioptra antDioptra = new Dioptra()
    private static boolean ticketRx
    private String armazonString = null
    private Boolean activeDialogProccesCustomer = true



    OrderPanel() {
        sb = new SwingBuilder()
        order = new Order()
        dioptra = new Dioptra()
        customer = CustomerController.findDefaultCustomer()
        promotionList = new ArrayList<PromotionAvailable>()
        this.promotionDriver.init(this)
        ticketRx = false
        customerTypes.remove(OperationType.DOMESTIC)
        buildUI()
        doBindings()
        itemsModel.addTableModelListener(this.promotionDriver)
        uiEnabled = true
        OperationType
    }

    private PromotionDriver getPromotionDriver() {
        return PromotionDriver.instance
    }

    private void buildUI() {
        sb.panel(this, layout: new MigLayout('insets 5,fill,wrap', '[fill]', '[fill]')) {
            panel(layout: new MigLayout('insets 0,fill', '[fill,260][fill,180][fill,300!]', '[fill]')) {
                panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][fill,220!]', '[top]')) {
                    label('Cliente')
                    customerName = button(enabled: false, actionPerformed: doCustomerSearch)

                    label('Tipo')
                    operationType = comboBox(items: customerTypes, itemStateChanged: operationTypeChanged)
                }

                panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][grow,right]', '[top]')) {
                    def displayFont = new Font('', Font.BOLD, 19)
                    label()
                    date = label(font: displayFont)
                    label('Folio')
                    folio = label()
                    label('Factura')
                    bill = label()
                    //label( 'Fecha' )
                }

                panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][grow,right]', '[top]')) {
                    def displayFont = new Font('', Font.BOLD, 22)
                    label('Venta')
                    total = label(font: displayFont)
                    label('Pagado')
                    paid = label(font: displayFont)
                    label('Saldo')
                    due = label(font: displayFont)
                }
            }

            itemSearch = textField(font: new Font('', Font.BOLD, 16), document: new UpperCaseDocument(), actionPerformed: { doItemSearch() })
            itemSearch.addFocusListener(this)

            scrollPane(border: titledBorder(title: 'Art\u00edculos')) {
                table(selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowItemClick) {
                    itemsModel = tableModel(list: order.items) {
                        closureColumn(
                                header: 'Art\u00edculo',
                                read: { OrderItem tmp -> "${tmp?.item?.name} ${tmp?.item?.color ?: ''}" },
                                minWidth: 80,
                                maxWidth: 100
                        )
                        closureColumn(
                                header: 'Descripci\u00f3n',
                                read: { OrderItem tmp -> tmp?.description }
                        )
                        closureColumn(
                                header: 'Cantidad',
                                read: { OrderItem tmp -> tmp?.quantity },
                                minWidth: 70,
                                maxWidth: 70
                        )
                        closureColumn(
                                header: 'Precio',
                                read: { OrderItem tmp -> tmp?.item?.price },
                                minWidth: 80,
                                maxWidth: 100,
                                cellRenderer: new MoneyCellRenderer()
                        )
                        closureColumn(
                                header: 'Total',
                                read: { OrderItem tmp -> tmp?.item?.price * tmp?.quantity },
                                minWidth: 80,
                                maxWidth: 100,
                                cellRenderer: new MoneyCellRenderer()
                        )
                    } as DefaultTableModel
                }
            }

            panel(layout: new MigLayout('insets 0,fill', '[fill][fill,240!]', '[fill]')) {
                scrollPane(border: titledBorder(title: "Promociones"),
                        mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions(ev) },
                        mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions(ev) }
                ) {
                    table(selectionMode: ListSelectionModel.SINGLE_SELECTION,
                            mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions(ev) },
                            mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions(ev) }
                    ) {
                        promotionModel = tableModel(list: promotionList) {
                            closureColumn(header: "", type: Boolean, maxWidth: 25,
                                    read: { row -> row.applied },
                                    write: { row, newValue ->
                                        onTogglePromotion(row, newValue)
                                    }
                            )
                            propertyColumn(header: "Descripci\u00f3n", propertyName: "description", editable: false)
                            propertyColumn(header: "Art\u00edculo", propertyName: "partNbrList", maxWidth: 100, editable: false)
                            closureColumn(header: "Precio Base",
                                    read: { IPromotionAvailable promotion -> promotion.baseAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                            closureColumn(header: "Descto",
                                    read: { IPromotionAvailable promotion -> promotion.discountAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                            closureColumn(header: "Promoci\u00f3n",
                                    read: { IPromotionAvailable promotion -> promotion.promotionAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                        } as DefaultTableModel
                    }
                }

                scrollPane(border: titledBorder(title: 'Pagos'), mouseClicked: doNewPaymentClick) {
                    table(selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowPaymentClick) {
                        paymentsModel = tableModel(list: order.payments) {
                            closureColumn(header: 'Descripci\u00f3n', read: { Payment tmp -> tmp?.description })
                            closureColumn(header: 'Monto', read: { Payment tmp -> tmp?.amount }, maxWidth: 100, cellRenderer: new MoneyCellRenderer())
                        } as DefaultTableModel
                    }
                }
            }

            scrollPane(border: titledBorder(title: 'Observaciones')) {
                comments = textArea(document: new UpperCaseDocument(), lineWrap: true)
            }

            // panel( layout: new MigLayout( 'insets 0,fill', '[fill,125!][fill,grow][fill,125!]', '[fill,40!]' ) ) {
            panel(minimumSize: [750, 45], border: BorderFactory.createEmptyBorder(0, 0, 0, 0)) {
                borderLayout()
                panel(constraints: BorderLayout.LINE_START, border: BorderFactory.createEmptyBorder(0, 0, 0, 0)) {
                    closeButton = button(TXT_BTN_CLOSE,
                            preferredSize: UI_Standards.BIG_BUTTON_SIZE,
                            actionPerformed: doClose
                    )
                }
                change = label(foreground: UI_Standards.WARNING_FOREGROUND, constraints: BorderLayout.CENTER)
                panel(constraints: BorderLayout.LINE_END, border: BorderFactory.createEmptyBorder(0, 0, 0, 0)) {
                    newOrderButton = button(TXT_BTN_NEW_ORDER,
                            preferredSize: UI_Standards.BIG_BUTTON_SIZE,
                            actionPerformed: { fireRequestNewOrder() }
                    )
                    quoteButton = button(TXT_BTN_QUOTE,
                            preferredSize: UI_Standards.BIG_BUTTON_SIZE,
                            actionPerformed: { fireRequestQuote() }
                    )
                    continueButton = button(TXT_BTN_CONTINUE,
                            preferredSize: UI_Standards.BIG_BUTTON_SIZE,
                            actionPerformed: { fireRequestContinue(itemsModel) }
                    )
                    printButton = button(TXT_BTN_PRINT,
                            preferredSize: UI_Standards.BIG_BUTTON_SIZE,
                            actionPerformed: doPrint
                    )
                }
            }
        }
    }

    private void doBindings() {
        sb.build {
            bean(customerName, text: bind { customer?.fullName })
            bean(folio, text: bind { order.id })
            bean(bill, text: bind { order.bill })
            bean(date, text: bind(source: order, sourceProperty: 'date', converter: dateConverter), alignmentX: CENTER_ALIGNMENT)
            bean(total, text: bind(source: order, sourceProperty: 'total', converter: currencyConverter))
            bean(paid, text: bind(source: order, sourceProperty: 'paid', converter: currencyConverter))
            bean(due, text: bind(source: order, sourceProperty: 'dueString'))
            bean(itemsModel.rowsModel, value: bind(source: order, sourceProperty: 'items', mutual: true))
            bean(paymentsModel.rowsModel, value: bind(source: order, sourceProperty: 'payments', mutual: true))
            bean(comments, text: bind(source: order, sourceProperty: 'comments', mutual: true))
            bean(order, customer: bind { customer })
        }
        itemsModel.fireTableDataChanged()
        paymentsModel.fireTableDataChanged()


        if (order?.id != null) {
            if (order?.dioptra != null) {
                dioptra = OrderController.generaDioptra(OrderController.preDioptra(order?.dioptra))
            }
           //  println('antDioptra: ' +  OrderController.codigoDioptra(antDioptra))
          //  println('Dioptra: ' +  OrderController.codigoDioptra(dioptra))
            change.text = OrderController.requestEmployee(order?.id)
        } else {

            change.text = ''
        }
        currentOperationType = (OperationType) operationType.getSelectedItem()
        this.printButton.setVisible(!this.isPaymentListEmpty())
        this.continueButton.setVisible(this.isPaymentListEmpty())
    }

    private void updateOrder(String pOrderId) {
        Order tmp = OrderController.getOrder(pOrderId)
        if (tmp?.id) {
            order = tmp
            doBindings()
        }
    }

    private def dateConverter = { Date val ->
        val?.format('dd-MM-yyyy')
    }

    private def currencyConverter = {
        NumberFormat.getCurrencyInstance(Locale.US).format(it ?: 0)
    }

    private def doCustomerSearch = { ActionEvent ev ->
        JButton source = ev.source as JButton
        source.enabled = false
        if (order.customer.id == null) {
            //CustomerController.browseCustomer(this)
            sb.doLater {
                if (this.customer == null) {
                    this.operationType.setSelectedItem(OperationType.DEFAULT)
                }
            }
        } else {
          if ( CustomerType.FOREIGN.equals( customer.type ) ) {
              ForeignCustomerDialog dialog = new ForeignCustomerDialog( this, customer, true )
              dialog.show()
              this.customer = dialog.customer
          } else {
              NewCustomerAndRxDialog dialog = new NewCustomerAndRxDialog( this, customer, true )
              dialog.show()
              this.customer = dialog.customer
          }
          //order.rx = CustomerController.queryCustomer(order.customer)
          sb.doLater {
              this.doBindings()
          }
        }

        doBindings()
        source.enabled = true
    }

    private def operationTypeChanged = { ItemEvent ev ->
        if (ev.stateChange == ItemEvent.SELECTED && this.uiEnabled) {
            switch (ev.item) {
                case OperationType.DEFAULT:
                    customer = CustomerController.findDefaultCustomer()

                    customerName.enabled = false
                    break
                case OperationType.WALKIN:
                    customer = new Customer(type: CustomerType.DOMESTIC)
                    ForeignCustomerDialog dialog = new ForeignCustomerDialog(ev.source as Component, customer, false)
                    dialog.show()
                    if (!dialog.canceled) {
                        customer = dialog.customer
                    }
                    break
                /*case OperationType.DOMESTIC:
                    customer = new Customer(type: CustomerType.DOMESTIC)
                    CustomerSearchDialog dialog = new CustomerSearchDialog(ev.source as Component, order)
                    dialog.show()
                    if (!dialog.canceled) {
                        customer = dialog.customer
                    }
                    break*/
                case OperationType.FOREIGN:
                    customer = new Customer(type: CustomerType.FOREIGN)
                    ForeignCustomerDialog dialog = new ForeignCustomerDialog(ev.source as Component, customer, false)
                    dialog.show()
                    if (!dialog.canceled) {
                        customer = dialog.customer
                    }
                    break
                case OperationType.QUOTE:
                    String orderNbr = OrderController.requestOrderFromQuote(this)
                    sb.doLater {
                        if (StringUtils.trimToNull(orderNbr) != null) {
                            Customer tmp = OrderController.getCustomerFromOrder(orderNbr)
                            if (tmp != null) {
                                customer = tmp
                            }
                            updateOrder(orderNbr)
                        } else {
                            operationType.setSelectedItem(currentOperationType)
                        }
                    }
                    break
                case OperationType.AGREEMENT:
                    operationType.setSelectedItem(OperationType.DEFAULT)
                    break
                case OperationType.NEW:

                    sb.doLater {
                        CustomerController.requestNewCustomer(this)

                    }

                    break
                case OperationType.PENDING:
                    sb.doLater {
                        if(activeDialogProccesCustomer){
                          CustomerController.requestPendingCustomer(this)
                        }
                        activeDialogProccesCustomer = true
                    }
                    break
                case OperationType.PAYING:
                    sb.doLater {
                        CustomerController.requestPayingCustomer(this)
                    }
                    break
            }
            if(!operationType.selectedItem.equals(OperationType.DOMESTIC)){
              operationType.removeItem( OperationType.DOMESTIC )
            }
            this.setCustomerInOrder()
            doBindings()
        } else {
            customerName.enabled = true
        }
    }


    private def doItemSearch() {
        Receta rec = new Receta()
        String input = itemSearch.text
        Boolean newOrder = false
        if (order?.id != null) {
            newOrder = StringUtils.isBlank(order.id)
        }
        if (StringUtils.isNotBlank(input)) {
            sb.doOutside {
                List<Item> results = ItemController.findItemsByQuery(input)
                if ((results.size() == 0) && (input.length() > 6)) {
                    results = ItemController.findItemsByQuery(input.substring(0, 6))
                }
                if (results?.any()) {
                    Item item = new Item()
                    if (results.size() == 1) {
                        item = results.first()
                        if( item.type.trim().equalsIgnoreCase(TAG_GENERICO_B) ){
                          if( customer.id != 1 ){
                            validarVentaNegativa(item, customer)
                          } else {
                            optionPane(message: "Cliente invalido, dar de alta datos", optionType: JOptionPane.DEFAULT_OPTION)
                                    .createDialog(new JTextField(), "Articulo Invalido")
                                    .show()
                          }
                        } else {
                          validarVentaNegativa(item, customer)
                        }
                    } else {
                        SuggestedItemsDialog dialog = new SuggestedItemsDialog(itemSearch, input, results)
                        dialog.show()
                        item = dialog.item
                        if (item?.id) {
                          if( item?.type.trim().equalsIgnoreCase(TAG_GENERICO_B) ){
                            if(customer.id != 1){
                              validarVentaNegativa(item, customer)
                            } else {
                              optionPane(message: "Cliente invalido, dar de alta datos", optionType: JOptionPane.DEFAULT_OPTION)
                                      .createDialog(new JTextField(), "Articulo Invalido")
                                      .show()
                            }
                          } else {
                            validarVentaNegativa(item, customer)
                          }
                        }
                    }
                } else {
                    optionPane(message: "No se encontraron resultados para: ${input}", optionType: JOptionPane.DEFAULT_OPTION)
                            .createDialog(new JTextField(), "B\u00fasqueda: ${input}")
                            .show()
                }
                if (newOrder && (StringUtils.trimToNull(order?.id) != null) && (StringUtils.trimToNull(customer?.id) != null)) {
                    this.setCustomerInOrder()
                }

            }
            sb.doLater {
                itemSearch.text = null
            }

        } else {
            sb.optionPane(message: 'Es necesario ingresar una b\u00fasqeda v\u00e1lida', optionType: JOptionPane.DEFAULT_OPTION)
                    .createDialog(new JTextField(), "B\u00fasqueda inv\u00e1lida")
                    .show()
        }
    }

    private def doShowItemClick = { MouseEvent ev ->
        if (SwingUtilities.isLeftMouseButton(ev)) {
            if (ev.clickCount == 2) {
                new ItemDialog(ev.component, order, ev.source.selectedElement, this).show()
                updateOrder(order?.id)

            }
        }
    }

    private def doNewPaymentClick = { MouseEvent ev ->
        if (SwingUtilities.isLeftMouseButton(ev)) {
            if (ev.clickCount == 1) {
                if (order.due) {
                    new PaymentDialog(ev.component, order, null).show()
                    updateOrder(order?.id)
                } else {
                    sb.optionPane(
                            message: 'No hay saldo para aplicar pago',
                            messageType: JOptionPane.ERROR_MESSAGE
                    ).createDialog(this, 'Pago sin saldo')
                            .show()
                }
            }
        }
    }

    private def doShowPaymentClick = { MouseEvent ev ->
        if (SwingUtilities.isLeftMouseButton(ev)) {
            if (ev.clickCount == 2) {
                new PaymentDialog(ev.component, order, ev.source.selectedElement).show()
                updateOrder(order?.id)
            }
        }
    }

    private void reviewForTransfers(String newOrderId) {
        if (CancellationController.orderHasTransfers(newOrderId)) {
            List<Order> lstOrders = CancellationController.findOrderToResetValues(newOrderId)
            for (Order order : lstOrders) {
                CancellationController.resetValuesofCancellation(order.id)
            }
            List<String> sources = CancellationController.findSourceOrdersWithCredit(newOrderId)
            if (sources?.any()) {
                new RefundDialog(this, sources.first()).show()
            } else {
                CancellationController.printCancellationsFromOrder(newOrderId)
            }
        }
    }

    private Receta validarGenericoB(Item item) {
        rec = null
        try {
            //Receta Nueva
            String artString = item.name
            if (artString.equals('SV') || artString.equals('P') || artString.equals('B')) {
                Branch branch = Session.get(SessionItem.BRANCH) as Branch
                EditRxDialog editRx = new EditRxDialog(this, new Rx(), customer?.id, branch?.id, 'Nueva Receta', item.description)
                editRx.show()

                this.disableUI()
                this.setCustomer(customer)
                this.setOrder(order)
                this.enableUI()

            } else {
                rec = null
                this.disableUI()
                this.setCustomer(customer)
                this.setOrder(order)
                this.enableUI()
            }
        } catch (ex) {
            rec = null
        }



        return rec
    }

    private SurteSwitch surteSu(Item item, SurteSwitch surteSwitch){
       if(surteSwitch?.surteSucursal==false){
        if(item?.type?.trim().equals('A') && item?.stock > 0 ){
            surteSwitch?.surteSucursal=true
        }else{
            AuthorizationDialog authDialog = new AuthorizationDialog(this, "Esta operacion requiere autorizaci\u00f3n")
            authDialog.show()
            println('Autorizado: ' + authDialog.authorized)
            if (authDialog.authorized) {
                surteSwitch?.surteSucursal=true
            } else {
                OrderController.notifyAlert('Se requiere autorizacion para esta operacion', 'Se requiere autorizacion para esta operacion')
            }
        }
       }
        return surteSwitch
    }

    private void validarVentaNegativa(Item item, Customer customer) {

        User u = Session.get(SessionItem.USER) as User
        order.setEmployee(u.username)
        Branch branch = Session.get(SessionItem.BRANCH) as Branch

        SurteSwitch surteSwitch = OrderController.surteCallWS(branch, item, 'S',order)
        surteSwitch = surteSu(item,surteSwitch)
        if (surteSwitch?.agregaArticulo == true && surteSwitch?.surteSucursal == true) {
             String surte = surteSwitch?.surte
            if (item.stock > 0) {
                order = OrderController.addItemToOrder(order, item, surte)
                controlItem(item)
                if (customer != null) {
                    order.customer = customer
                }
            } else {
                SalesWithNoInventory onSalesWithNoInventory = OrderController.requestConfigSalesWithNoInventory()
                order.customer = customer
                if (SalesWithNoInventory.ALLOWED.equals(onSalesWithNoInventory)) {
                    order = OrderController.addItemToOrder(order, item, surte)
                    controlItem(item)
                } else if (SalesWithNoInventory.REQUIRE_AUTHORIZATION.equals(onSalesWithNoInventory)) {
                    boolean authorized
                    if (AccessController.authorizerInSession) {
                        authorized = true
                    } else {
                        AuthorizationDialog authDialog = new AuthorizationDialog(this, "Cancelaci\u00f3n requiere autorizaci\u00f3n")
                        authDialog.show()
                        authorized = authDialog.authorized
                    }
                    if (authorized) {
                        order = OrderController.addItemToOrder(order, item, surte)
                        controlItem(item)
                    }
                } else {
                    sb.optionPane(message: MSJ_VENTA_NEGATIVA, messageType: JOptionPane.ERROR_MESSAGE,)
                            .createDialog(this, TXT_VENTA_NEGATIVA_TITULO)
                            .show()
                }
            }
        }
    }

    private def doClose = {
        sb.doLater {
            doBindings()
            if (order.payments.size() == 0) {
                Integer question = JOptionPane.showConfirmDialog(new JDialog(), MSJ_CAMBIAR_VENDEDOR, TXT_CAMBIAR_VENDEDOR,
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
                if (question == 0) {
                    MainWindow.instance.requestLogout()
                }
            } else {
                sb.optionPane(message: MSJ_QUITAR_PAGOS, messageType: JOptionPane.INFORMATION_MESSAGE,)
                        .createDialog(this, TXT_QUITAR_PAGOS)
                        .show()
            }
        }
    }

    private def doPrint = { ActionEvent ev ->
        int artCount = 0
        dioptra = OrderController.generaDioptra(OrderController.preDioptra(order?.dioptra))
        String dio = OrderController.codigoDioptra(dioptra)
        if (!dioptra.getLente().equals(null)) {
            Item i = OrderController.findArt(dio.trim())
            if (i?.id != null || dio.trim().equals('nullnullnullnullnullnull')) {
                String tipoArt = null
                for (int row = 0; row <= itemsModel.rowCount; row++) {
                    String artString = itemsModel.getValueAt(row, 0).toString()
                    if (artString.trim().equals('SV')) {
                        artCount = artCount + 1
                        tipoArt = 'MONOFOCAL'
                    } else if (artString.trim().equals('B')) {
                        artCount = artCount + 1
                        tipoArt = 'BIFOCAL'
                    } else if (artString.trim().equals('P')) {
                        artCount = artCount + 1
                        tipoArt = 'PROGRESIVO'
                    }
                }
                armazonString = OrderController.armazonString(order?.id)

                if (artCount == 0) {
                    JButton source = ev.source as JButton
                    source.enabled = false
                    ticketRx = false
                    flujoImprimir(artCount)
                    source.enabled = true
                } else {
                    rec = OrderController.findRx(order, customer)
                    Order armOrder = OrderController.getOrder(order?.id)
                    if (rec.id == null) {   //Receta Nueva
                        Branch branch = Session.get(SessionItem.BRANCH) as Branch
                        EditRxDialog editRx = new EditRxDialog(this, new Rx(), customer?.id, branch?.id, 'Nueva Receta', tipoArt)
                        editRx.show()
                        try {
                            OrderController.saveRxOrder(order?.id, rec.id)
                            JButton source = ev.source as JButton
                            source.enabled = false
                            ticketRx = true
                            if (armOrder?.udf2.equals('')) {
                                ArmRxDialog armazon = new ArmRxDialog(this, order, armazonString)
                                armazon.show()
                                order = armazon.order
                            }
                            flujoImprimir(artCount)
                            source.enabled = true
                        } catch ( Exception e) { println e }
                    } else {
                        JButton source = ev.source as JButton
                        source.enabled = false
                        ticketRx = true
                        if (armOrder?.udf2.equals('')) {
                            ArmRxDialog armazon = new ArmRxDialog(this, order, armazonString)
                            armazon.show()
                           order = armazon.order
                        }
                        flujoImprimir(artCount)
                        source.enabled = true
                    }
                }
            } else {
                sb.optionPane(message: "Codigo Dioptra Incorrecto", optionType: JOptionPane.DEFAULT_OPTION)
                        .createDialog(new JTextField(), "Error")
                        .show()
            }
        } else {
            ticketRx = false
            flujoImprimir(artCount)
        }
    }

    private void controlItem(Item item){
        Branch branch = Session.get(SessionItem.BRANCH) as Branch
        OrderController.insertaAcuseAPAR(order,branch,item)
        String indexDioptra = item?.indexDiotra
        println('Index Dioptra del Articulo : ' +item?.indexDiotra)
        if (!indexDioptra.equals(null)) {
            Dioptra nuevoDioptra = OrderController.generaDioptra(item?.indexDiotra)
            println('Nuevo Objeto Dioptra :' + nuevoDioptra)
            dioptra = OrderController.validaDioptra(dioptra, nuevoDioptra)
            println('Dioptra Generado :' + dioptra)
            antDioptra = OrderController.addDioptra(order, OrderController.codigoDioptra(dioptra))
            order?.dioptra = OrderController.codigoDioptra(antDioptra)
        } else {
            order?.dioptra = OrderController.codigoDioptra(antDioptra)
        }
        println('Codigo Dioptra :' + antDioptra)


        rec = validarGenericoB(item)
        OrderController.saveRxOrder(order?.id, rec.id)
        updateOrder(order?.id)
        if (!order.customer.equals(customer)) {
            order.customer = customer
        }
    }

    private void flujoImprimir(int artCount) {
        armazonString = null
        Boolean validOrder = isValidOrder()
        if (artCount != 0) {
            Parametro diaIntervalo = Registry.find(TipoParametro.DIA_PRO)
            Date diaPrometido = new Date() + diaIntervalo?.valor.toInteger()
            OrderController.savePromisedDate(order?.id, diaPrometido)
            Double pAnticipo = Registry.getAdvancePct()
            if (order?.paid < (order?.total * pAnticipo)) {
                AuthorizationDialog authDialog = new AuthorizationDialog(this, "Anticipo menor al permitido, esta operacion requiere autorizaci\u00f3n")
                authDialog.show()
                if (authDialog.authorized) {
                    validOrder = isValidOrder()
                } else {
                    validOrder = false
                    sb.optionPane(
                            message: 'El monto del anticipo tiene que ser minimo de: $' + (order?.total * pAnticipo),
                            messageType: JOptionPane.ERROR_MESSAGE
                    ).createDialog(this, 'No se puede registrar la venta')
                            .show()
                }
            } else {
                validOrder = isValidOrder()
            }
        }
        if (validOrder) {
            /*if (operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.WALKIN.value) ||
                    operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.DOMESTIC.value)) {
                //order.country = 'MEXICO'
                saveOrder()
            } else if (operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.FOREIGN.value)) {
                String paisCliente = CustomerController.countryCustomer(order)
                if (paisCliente.length() > 0) {
                    //order.country = paisCliente
                    saveOrder()
                } else {
                    CountryCustomerDialog dialog = new CountryCustomerDialog(MainWindow.instance)
                    dialog.show()
                    if (dialog.button == true) {
                        order.country = dialog.pais
                        saveOrder()
                    }
                }
            } else if (operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.DEFAULT.value)) {
                CountryCustomerDialog dialog = new CountryCustomerDialog(MainWindow.instance)
                dialog.show()
                if (dialog.button == true) {
                    order.country = dialog.pais
                    saveOrder()
                }
            } else if (operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.PAYING.value)) {*/
            doBindings()
            saveOrder()
            //}
        }

    }

    private void saveOrder() {
        Order newOrder = OrderController.placeOrder(order)
        //CustomerController.saveOrderCountries(order.country)
        this.promotionDriver.requestPromotionSave(newOrder?.id)
        Boolean cSaldo = false
        // if(newOrder?.due > 0){
        //   cSaldo = true
        // }
        OrderController.creaJb(newOrder?.ticket.trim(), cSaldo)
        OrderController.validaEntrega(newOrder?.bill.trim(),newOrder?.branch?.id.toString(), true)
        OrderController.validaSurtePorGenerico( order )
        if (StringUtils.isNotBlank(newOrder?.id)) {
            OrderController.printOrder(newOrder.id)
            if (ticketRx == true) {
                OrderController.printRx(newOrder.id, false)
                OrderController.fieldRX(newOrder.id)
            }
            reviewForTransfers(newOrder.id)
            // Flujo despues de imprimir nota de venta

            CustomerController.requestOrderByCustomer(this, customer)
            // Flujo despues de imprimir nota de venta
        } else {
            sb.optionPane(
                    message: 'Ocurrio un error al registrar la venta, intentar nuevamente',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog(this, 'No se puede registrar la venta')
                    .show()
        }
    }

    private boolean isValidOrder() {
        if (itemsModel.size() == 0) {
            sb.optionPane(
                    message: 'Se debe agregar al menos un art\u00edculo a la venta',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog(this, 'No se puede registrar la venta')
                    .show()
            return false
        }
        if (!OrderController.isPaymentPolicyFulfilled(order)) {

            return false
        }


        return true
    }




    protected void onMouseClickedAtPromotions(MouseEvent pEvent) {
        // rld Strange no mouse event triggers popup menu
        // change isPopupTrigger to MouseClicked and button = 3
        // if ( pEvent.isPopupTrigger() ) {
        if (SwingUtilities.isRightMouseButton(pEvent) && (pEvent.getID() == MouseEvent.MOUSE_CLICKED)) {
            if (discountMenu == null) {
                discountMenu = new DiscountContextMenu(this.promotionDriver)
            }
            discountMenu.activate(pEvent)
        }
    }

    protected void onTogglePromotion(IPromotionAvailable pPromotion, Boolean pNewValue) {
        if (pNewValue) {
            this.promotionDriver.requestApplyPromotion(pPromotion)
        } else {
            this.promotionDriver.requestCancelPromotion(pPromotion)
            //promotionList.remove( 0 )
        }
    }

    Order getOrder() {
        return order
    }

    public List<IPromotionAvailable> getPromotionList() {
        return this.promotionList
    }

    DefaultTableModel getPromotionModel() {
        return this.promotionModel
    }

    void refreshData() {
        this.promotionDriver.enableItemsTableEvents(false)
        this.getPromotionModel().fireTableDataChanged()
        updateOrder(order?.id)
        this.promotionDriver.enableItemsTableEvents(true)
    }

    public void focusGained(FocusEvent e) {

    }

    public void focusLost(FocusEvent e) {
        if (itemSearch.text.length() > 0) {
            doItemSearch()
            itemSearch.requestFocus()
        }
    }

    private void fireRequestQuote() {
        dioptra = OrderController.generaDioptra(OrderController.preDioptra(order?.dioptra))
        String dio = OrderController.codigoDioptra(dioptra)
        if (dioptra.getLente() != null) {
            Item i = OrderController.findArt(dio.trim())
            if (i?.id != null) {
                if (itemsModel.size() > 0) {
                    if (paymentsModel.size() == 0) {
                        OrderController.requestSaveAsQuote(order, customer)
                        this.reset()
                    } else {
                        sb.doLater {
                            OrderController.notifyAlert(TXT_REQUEST_QUOTE, TXT_PAYMENTS_PRESENT)
                        }
                    }
                } else {
                    sb.doLater {
                        OrderController.notifyAlert(TXT_REQUEST_QUOTE, TXT_NO_ORDER_PRESENT)
                    }
                }
            } else {
                sb.optionPane(message: "Codigo Dioptra Incorrecto", optionType: JOptionPane.DEFAULT_OPTION)
                        .createDialog(new JTextField(), "Error")
                        .show()
            }
        } else {
            flujoContinuar()
        }
    }

    private void setCustomerInOrder() {
        if ((order?.id != null) && (customer != null)) {
            if (!order.customer.equals(customer)) {
                order.customer = customer
                OrderController.saveCustomerForOrder(order.id, customer.id)
            }
        }
    }

    void setCustomerInOrderFromMenu( Customer customer ) {
        if ((order?.id != null) && (customer != null)) {
            if (!order.customer.equals(customer)) {
                order.customer = customer
                OrderController.saveCustomerForOrder(order.id, customer.id)
            }
        }
        this.customer = customer
        if(!operationType.selectedItem.equals(OperationType.DOMESTIC) ){
          operationType.addItem( OperationType.DOMESTIC )
        }
        if(this.customer != null){
          if( CustomerController.findProccesClient(this.customer.id) != null ){
            activeDialogProccesCustomer = false
            operationType.setSelectedItem( OperationType.PENDING )
          } else {
            operationType.setSelectedItem( OperationType.DOMESTIC )
          }
        } else {
          operationType.removeItem( OperationType.DOMESTIC )
        }
        doBindings()
    }

    void reset() {

        order = new Order()
        customer = CustomerController.findDefaultCustomer()
        // Benja: Favor de no cambiar la siguiente linea. Esta comentada porque NO debe de estar
        // this.promotionList = new ArrayList<PromotionAvailable>()
        this.getPromotionDriver().init(this)
        dioptra = new Dioptra()
        antDioptra = new Dioptra()
        order?.dioptra = null
        doBindings()
        operationType.setSelectedItem(OperationType.DEFAULT)
    }

    void setCustomer(Customer pCustomer) {
        this.logger.debug(String.format('Assign Customer: %s', pCustomer.toString()))

        customer = pCustomer
        doBindings()
    }

    void setOrder(Order pOrder) {
        this.logger.debug(String.format('Assign Order: %s', pOrder.toString()))
        this.updateOrder(pOrder.id)
    }

    void setOperationTypeSelected(OperationType pOperation) {
        operationType.setSelectedItem(pOperation)
    }

    void disableUI() {
        uiEnabled = false
    }

    void enableUI() {
        this.doBindings()
        uiEnabled = true
    }

    private void fireRequestContinue(DefaultTableModel itemsModel) {

        dioptra = OrderController.generaDioptra(OrderController.preDioptra(order?.dioptra))
        String dio = OrderController.codigoDioptra(dioptra)
        if (!dioptra.getLente().equals(null)) {

            Item i = OrderController.findArt(dio.trim())

            if (i?.id != null || dio.trim().equals('nullnullnullnullnullnull')) {
                String tipoArt = null
                int artCount = 0
                for (int row = 0; row <= itemsModel.rowCount; row++) {
                    String artString = itemsModel.getValueAt(row, 0).toString()
                    if (artString.trim().equals('SV')) {
                        artCount = artCount + 1
                        tipoArt = 'MONOFOCAL'
                    } else if (artString.trim().equals('B')) {
                        artCount = artCount + 1
                        tipoArt = 'BIFOCAL'
                    } else if (artString.trim().equals('P')) {
                        artCount = artCount + 1
                        tipoArt = 'PROGRESIVO'
                    }
                }
                if (artCount == 0) {
                    flujoContinuar()

                } else {
                    rec = OrderController.findRx(order, customer)
                    Order armOrder = OrderController.getOrder(order?.id)


                    if (rec.id == null) {   //Receta Nueva
                        Branch branch = Session.get(SessionItem.BRANCH) as Branch

                        EditRxDialog editRx = new EditRxDialog(this, new Rx(), customer?.id, branch?.id, 'Nueva Receta', tipoArt)
                        editRx.show()

                        try {
                            OrderController.saveRxOrder(order?.id, rec.id)
                           /*
                            if (armOrder?.udf2.equals('')) {
                                ArmRxDialog armazon = new ArmRxDialog(this, order?.id, armazonString)
                                armazon.show()
                            }
                             */
                            flujoContinuar()
                        } catch (ex) {
                            flujoContinuar()
                        }


                    } else {    //Receta ya Capturada
                        /*
                           Rx rx = Rx.toRx(rec)
                           Branch branch = Session.get( SessionItem.BRANCH ) as Branch
                           EditRxDialog editRx = new EditRxDialog( this, rx, customer?.id, branch?.id, 'Nueva Receta',tipoArt )
                           editRx.show()


                        if (armOrder?.udf2.equals('')) {
                            ArmRxDialog armazon = new ArmRxDialog(this, order?.id, armazonString)
                            armazon.show()
                        }
                          */
                        flujoContinuar()

                    }


                }

            } else {
                sb.optionPane(message: "Codigo Dioptra Incorrecto", optionType: JOptionPane.DEFAULT_OPTION)
                        .createDialog(new JTextField(), "Error")
                        .show()
            }

        } else {
            flujoContinuar()
        }
    }

    private void flujoContinuar() {

        if (isPaymentListEmpty()) {


            sb.doLater {

                OrderController.saveOrder(order)
                CustomerController.updateCustomerInSite(this.customer.id)

                this.reset()


            }
        } else {
            sb.doLater {
                OrderController.notifyAlert(TXT_REQUEST_CONTINUE, TXT_PAYMENTS_PRESENT)
            }
        }
    }


    private void fireRequestNewOrder() {
        if (itemsModel.size() > 0) {
            if (paymentsModel.size() == 0) {
                Customer c = this.customer
                OrderController.saveOrder(order)
                CustomerController.updateCustomerInSite(c.id)
                this.reset()
                this.disableUI()
                this.operationTypeSelected = OperationType.PENDING
                this.setCustomer(c)
                this.enableUI()
            } else {
                sb.doLater {
                    OrderController.notifyAlert(TXT_REQUEST_NEW_ORDER, TXT_PAYMENTS_PRESENT)
                }
            }
        } else {
            sb.doLater {
                OrderController.notifyAlert(TXT_REQUEST_NEW_ORDER, TXT_PAYMENTS_PRESENT)
            }
        }
    }

    private Boolean isPaymentListEmpty() {
        return (order.payments.size() == 0)
    }


}
