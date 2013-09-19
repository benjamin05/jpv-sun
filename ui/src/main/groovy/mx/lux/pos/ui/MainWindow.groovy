package mx.lux.pos.ui

import groovy.swing.SwingBuilder
import mx.lux.pos.service.PromotionService
import mx.lux.pos.ui.model.Branch
import mx.lux.pos.ui.model.Order
import mx.lux.pos.ui.model.Session
import mx.lux.pos.ui.model.SessionItem
import mx.lux.pos.ui.model.User
import mx.lux.pos.ui.resources.ServiceManager
import mx.lux.pos.ui.view.action.ExitAction
import mx.lux.pos.ui.view.dialog.ChangePasswordDialog
import mx.lux.pos.ui.view.dialog.CustomerSearchDialog
import mx.lux.pos.ui.view.dialog.EntregaTrabajoDialog
import mx.lux.pos.ui.view.dialog.PartClassDialog
import net.miginfocom.swing.MigLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

import mx.lux.pos.ui.controller.*
import mx.lux.pos.ui.view.panel.*

import java.awt.*
import javax.swing.*
import mx.lux.pos.service.business.Registry
import mx.lux.pos.model.TipoParametro

import java.text.SimpleDateFormat

class MainWindow extends JFrame implements KeyListener {

    static final String MSG_LOAD_PARTS = "No se encuentra el archivo: %s"
    static final String TEXT_LOAD_PARTS_TITLE = "Importar Catálogo de Artículos"
    static final String TEXT_LOAD_PART_CLASS_TITLE = "Importar Clasificación de Artículos"
    static SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy")

    static String version

    static MainWindow instance
    private Logger log = LoggerFactory.getLogger( this.getClass() )
    private SwingBuilder sb
    private JPanel mainPanel
    private JPanel logInPanel
    private JPanel orderPanel
    private JPanel showOrderPanel
    private InvTrView invTrView
    private CustomerSearchDialog customerDialog
    private JPanel dailyClosePanel
    private JPanel priceListPanel
    private JPanel invoicePanel
    private JToolBar infoBar
    private JLabel userLabel
    private JLabel branchLabel
    private JLabel versionLabel
    private JMenu toolsMenu
    private JMenu ordersMenu
    private JMenu clientsMenu
    private JMenu inventoryMenu
    private JMenu reportsMenu
    private JMenuItem orderMenuItem
    private JMenuItem orderSearchMenuItem
    private JMenuItem dailyCloseMenuItem
    private JMenuItem priceListMenuItem
    private JMenuItem invoiceMenuItem
    private JMenuItem sessionMenuItem
    private JMenuItem cancellationReportMenuItem
    private JMenuItem dailyCloseReportMenuItem
    private JMenuItem incomePerBranchReportMenuItem
    private JMenuItem sellerRevenueReportMenuItem
    private JMenuItem undeliveredJobsReportMenuItem
    private JMenuItem salesReportMenuItem
    private JMenuItem salesByLineReportMenuItem
    private JMenuItem salesByBrandReportMenuItem
    private JMenuItem salesBySellerReportMenuItem
    private JMenuItem inventoryTransactionMenuItem
    private JMenuItem inventoryOhQueryMenuItem
    private JMenuItem salesBySellerByBrandMenuItem
    private JMenuItem stockbyBrandMenuItem
    private JMenuItem stockbyBrandColorMenuItem
    private JMenuItem taxBillsMenuItem
    private JMenuItem discountsMenuItem
    private JMenuItem promotionsMenuItem
    private JMenuItem promotionsListMenuItem
    private JMenuItem paymentsMenuItem
    private JMenuItem quoteMenuItem
    private JMenuItem loadPartsMenuItem
    private JMenuItem loadPartClassMenuItem
    private JMenuItem generateInventoryFile
    private JMenuItem newSalesDayMenuItem
    private JMenuItem cotizacionMenuItem
    private JMenuItem kardexMenuItem
    private JMenuItem salesTodayMenuItem
    private JMenuItem salesByPeriodMenuItem
    private JMenuItem entregaMenuItem
    private JMenuItem changePasswordMenuItem
    private JMenuItem nationalClientMenuItem
    private JMenuItem jobControlMenuItem
    private JMenuItem workSubmittedMenuItem
    private PromotionService promotionService


    MainWindow( ) {
        instance = this
        this.addKeyListener( this )
        sb = new SwingBuilder()
        buildUI()
    }

    private void buildUI( ) {
        logInPanel = new LogInPanel( doForwardToDefaultPanel, version )
        sb.build {
            lookAndFeel( 'system' )
            frame( this,
                    title: 'Punto de Venta',
                    focusable: true,
                    layout: new MigLayout( 'fill,insets 1,center,wrap', '[fill]', '[top]' ),
                    minimumSize: [ 850, 620 ] as Dimension,
                    location: [ 70, 35 ] as Point,
                    pack: true,
                    resizable: false,
                    defaultCloseOperation: EXIT_ON_CLOSE
            ) {
                menuBar {
                    ordersMenu = menu( text: 'Ventas', mnemonic: 'V',
                            menuSelected: {
                                boolean userLoggedIn = Session.contains( SessionItem.USER )
                                orderMenuItem.visible = userLoggedIn
                                orderSearchMenuItem.visible = userLoggedIn
                                //dailyCloseMenuItem.visible = userLoggedIn
                                //priceListMenuItem.visible = userLoggedIn
                                //invoiceMenuItem.visible = userLoggedIn
                                nationalClientMenuItem.visible = userLoggedIn
                                // TODO: Benja enable feature cotizacionMenuItem.visible = userLoggedIn
                            }
                    ) {
                        orderMenuItem = menuItem( text: 'Venta',
                                visible: false,
                                actionPerformed: {
                                    mainPanel.remove( orderPanel )
                                    orderPanel = new OrderPanel()
                                    mainPanel.add( 'orderPanel', orderPanel )
                                    mainPanel.layout.show( mainPanel, 'orderPanel' )
                                }
                        )
                        orderSearchMenuItem = menuItem( text: 'Consulta',
                                visible: false,
                                actionPerformed: {
                                    mainPanel.remove( showOrderPanel )
                                    showOrderPanel = new ShowOrderPanel()
                                    mainPanel.add( 'showOrderPanel', showOrderPanel )
                                    mainPanel.layout.show( mainPanel, 'showOrderPanel' )
                                }
                        )
                        cotizacionMenuItem = menuItem( text: 'Cotizaciones',
                                visible: false,
                                actionPerformed: { QuoteController.instance.requestQuote() }
                        )
                        /*dailyCloseMenuItem = menuItem( text: 'Cierre diario',
                                visible: false,
                                actionPerformed: {
                                    dailyClosePanel = new DailyClosePanel()
                                    mainPanel.add( 'dailyClosePanel', dailyClosePanel )
                                    mainPanel.layout.show( mainPanel, 'dailyClosePanel' )
                                }
                        )
                        priceListMenuItem = menuItem( text: 'Lista de Precios',
                                visible: false,
                                actionPerformed: {
                                    priceListPanel = new PriceListPanel().panel
                                    mainPanel.add( 'priceListPanel', priceListPanel )
                                    mainPanel.layout.show( mainPanel, 'priceListPanel' )
                                }
                        )
                        invoiceMenuItem = menuItem( text: 'Facturaci\u00f3n',
                                visible: false,
                                actionPerformed: {
                                    invoicePanel = new InvoicePanel()
                                    mainPanel.add( 'invoicePanel', invoicePanel )
                                    mainPanel.layout.show( mainPanel, 'invoicePanel' )
                                }
                        )*/
                    }
                    clientsMenu = menu( text: 'Clientes', mnemonic: 'C',
                            menuSelected: {
                                boolean userLoggedIn = Session.contains( SessionItem.USER )
                                nationalClientMenuItem.visible = userLoggedIn
                            }
                    ){
                      nationalClientMenuItem = menuItem( text: "Cliente Nacional", visible: true,
                              actionPerformed: { if ( customerDialog == null ) {
                                  customerDialog = new CustomerSearchDialog( this, new Order() )
                              }
                                  customerDialog.show()
                                  if (!customerDialog.canceled) {
                                    if( orderPanel.order.id == null ){
                                      mainPanel.remove( orderPanel )
                                      orderPanel = new OrderPanel()
                                      mainPanel.add( 'orderPanel', orderPanel )
                                      mainPanel.layout.show( mainPanel, 'orderPanel' )
                                    }
                                    orderPanel.setCustomerInOrderFromMenu( customerDialog.customer )
                                    customerDialog = new CustomerSearchDialog( this, new Order() )
                                  }
                                  customerDialog = new CustomerSearchDialog( this, new Order() )
                              }
                      )
                    }
                    inventoryMenu = menu( text: 'Inventario', mnemonic: 'I',
                            menuSelected: {
                                boolean userLoggedIn = Session.contains( SessionItem.USER )
                                inventoryTransactionMenuItem.visible = userLoggedIn
                                inventoryOhQueryMenuItem.visible = userLoggedIn
                                //generateInventoryFile.visible = userLoggedIn
                                //loadPartsMenuItem.visible = userLoggedIn
                                //loadPartClassMenuItem.visible = userLoggedIn
                            }
                    ) {
                        inventoryTransactionMenuItem = menuItem( text: 'Transacciones',
                                visible: false,
                                actionPerformed: {
                                    if ( invTrView == null ) {
                                        invTrView = new InvTrView()
                                    }
                                    mainPanel.add( 'invTrPanel', invTrView.panel )
                                    invTrView.activate()
                                    mainPanel.layout.show( mainPanel, 'invTrPanel' )
                                }
                        )
                        inventoryOhQueryMenuItem = menuItem( text: "Ticket Existencias", visible: true,
                                actionPerformed: { InvQryController.instance.requestInvOhTicket() }
                        )
                        /*loadPartsMenuItem = menuItem( text: TEXT_LOAD_PARTS_TITLE,
                                visible: true,
                                actionPerformed: {
                                    requestImportPartMaster()
                                }
                        )
                        loadPartClassMenuItem = menuItem( text: TEXT_LOAD_PART_CLASS_TITLE,
                                visible: true,
                                actionPerformed: {
                                    requestImportPartClass()
                                }
                        )
                        generateInventoryFile = menuItem( text: 'Archivo Inventario',
                                visible: false,
                                actionPerformed: {
                                    generateInventoryFile()
                                }
                        )*/
                    }
                    reportsMenu = menu( text: "Reportes", mnemonic: "R",
                            menuSelected: {
                                boolean userLoggedIn = Session.contains( SessionItem.USER )
                                cancellationReportMenuItem.visible = userLoggedIn
                                dailyCloseReportMenuItem.visible = userLoggedIn
                                //incomePerBranchReportMenuItem.visible = userLoggedIn
                                sellerRevenueReportMenuItem.visible = userLoggedIn
                                undeliveredJobsReportMenuItem.visible = userLoggedIn
                                salesReportMenuItem.visible = userLoggedIn
                                salesByLineReportMenuItem.visible = userLoggedIn
                                //salesBySellerReportMenuItem.visible = userLoggedIn
                                //salesByBrandReportMenuItem.visible = userLoggedIn
                                //salesBySellerByBrandMenuItem.visible = userLoggedIn
                                stockbyBrandMenuItem.visible = userLoggedIn
                                stockbyBrandColorMenuItem.visible = userLoggedIn
                                jobControlMenuItem.visible = userLoggedIn
                                workSubmittedMenuItem.visible = userLoggedIn
                                taxBillsMenuItem.visible = userLoggedIn
                                discountsMenuItem.visible = userLoggedIn
                                promotionsMenuItem.visible = userLoggedIn
                                //promotionsListMenuItem.visible = userLoggedIn
                                paymentsMenuItem.visible = userLoggedIn
                                quoteMenuItem.visible = userLoggedIn
                                kardexMenuItem.visible = userLoggedIn
                                //salesTodayMenuItem.visible = userLoggedIn
                                //salesByPeriodMenuItem.visible = userLoggedIn
                                undeliveredJobsReportMenuItem.visible = userLoggedIn
                                //examsMenuItem.visible = userLoggedIn
                                //optometristSalesMenuItem.visible = userLoggedIn
                            }
                    ) {
                        cancellationReportMenuItem = menuItem( text: "Cancelaciones",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Cancellations )
                                }
                        )
                        dailyCloseReportMenuItem = menuItem( text: "Cierre Diario",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.DailyClose )
                                }
                        )
                        jobControlMenuItem = menuItem( text: "Control de Trabajos",
                            visible: false,
                            actionPerformed: {
                              ReportController.fireReport( ReportController.Report.JobControl )
                            }
                        )
                        quoteMenuItem = menuItem( text: "Cotizaciones",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Quote )
                                }
                        )
                        discountsMenuItem = menuItem( text: "Descuentos",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Discounts )
                                }
                        )
                        stockbyBrandColorMenuItem = menuItem( text: "Existencias por Art\u00edculo",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.StockbyBrandColor )
                                }
                        )
                        stockbyBrandMenuItem = menuItem( text: "Existencias por Marca",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.StockbyBrand )
                                }
                        )
                        taxBillsMenuItem = menuItem( text: "Facturas Fiscales",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.TaxBills )
                                }
                        )
                        /*salesByPeriodMenuItem = menuItem( text: "Ingresos por Periodo",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.PaymentsbyPeriod )
                                }
                        )
                        incomePerBranchReportMenuItem = menuItem( text: "Ingresos por Sucursal",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.IncomePerBranch )
                                }
                        )*/
                        sellerRevenueReportMenuItem = menuItem( text: "Ingresos por Vendedor",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SellerRevenue )
                                }
                        )
                        kardexMenuItem = menuItem( text: "Kardex por Articulo",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Kardex )
                                }
                        )
                        paymentsMenuItem = menuItem( text: "Pagos",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Payments )
                                }
                        )
                        promotionsMenuItem = menuItem( text: "Promociones en Ventas",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.PromotionsinSales )
                                }
                        )
                        /*promotionsListMenuItem = menuItem( text: "Promociones",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Promotions )
                                }
                        )*/
                        workSubmittedMenuItem = menuItem( text: "Trabajos Entregados",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.WorkSubmitted )
                                }
                        )
                        undeliveredJobsReportMenuItem = menuItem( text: "Trabajos sin Entregar",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.UndeliveredJobs )
                                }
                        )
                        salesReportMenuItem = menuItem( text: "Ventas",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.Sales )
                                }
                        )
                        /*salesTodayMenuItem = menuItem( text: "Ventas del D\u00eda",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SalesToday )
                                }
                        )*/
                        salesByLineReportMenuItem = menuItem( text: "Ventas por L\u00ednea",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SalesbyLine )
                                }
                        )
                        /*salesByBrandReportMenuItem = menuItem( text: "Ventas por Marca",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SalesbyBrand )
                                }
                        )
                        salesBySellerReportMenuItem = menuItem( text: "Ventas por Vendedor",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SalesbySeller )
                                }
                        )
                        salesBySellerByBrandMenuItem = menuItem( text: "Ventas por Vendedor por Marca",
                                visible: false,
                                actionPerformed: {
                                    ReportController.fireReport( ReportController.Report.SalesbySellerbyBrand )
                                }
                        )*/
                    }
                    toolsMenu = menu( text: 'Herramientas', mnemonic: 'H',
                            menuSelected: {
                                boolean userLoggedIn = Session.contains( SessionItem.USER )
                                sessionMenuItem.visible = userLoggedIn
                                newSalesDayMenuItem.visible = userLoggedIn
                                entregaMenuItem.visible = userLoggedIn
                                changePasswordMenuItem.visible = userLoggedIn
                            }
                    ) {
                        entregaMenuItem = menuItem(text: 'Entrega',
                                visible: true,
                                actionPerformed: {
                                    entrega()
                                }
                        )
                        changePasswordMenuItem = menuItem( text: 'Cambio de Password',
                                visible: true,
                                actionPerformed: {
                                    ChangePasswordDialog dialog = new ChangePasswordDialog()
                                    dialog.show()
                                }
                        )
                        newSalesDayMenuItem = menuItem( text: 'Registrar Efectivo Caja',
                                visible: true,
                                actionPerformed: {
                                    requestNewSalesDay()
                                }
                        )
                        sessionMenuItem = menuItem( text: 'Cerrar Sesi\u00f3n',
                                visible: false,
                                actionPerformed: {
                                    requestLogout()
                                }
                        )
                        menuItem( action: new ExitAction().action )
                    }
                }
                infoBar = toolBar(
                        visible: false,
                        floatable: false,
                        background: Color.WHITE,
                        constraints: 'hidemode 3'
                ) {
                    borderLayout()
                    userLabel = label( constraints: BorderLayout.LINE_START )
                    branchLabel = label( constraints: BorderLayout.CENTER, horizontalAlignment: JLabel.CENTER_ALIGNMENT )
                    versionLabel = label( constraints: BorderLayout.LINE_END )
                }

                mainPanel = panel( layout: new CardLayout() )
                mainPanel.add( 'logInPanel', logInPanel )

            }
        }
    }

    public void keyReleased( KeyEvent e ) {
    }

    public void keyTyped( KeyEvent e ) {
    }

    public void keyPressed( KeyEvent e ) {
        if ( e.isControlDown() && e.getKeyChar() != 'k' && e.getKeyCode() == 75 ) {
            // TODO: Benjamin enable through feature
            // log.debug( "Abriendo dialogo de cotizaciones" )
            // QuoteController.instance.requestQuote()
        }
    }

    private def doForwardToDefaultPanel = {
        User user = Session.get( SessionItem.USER ) as User
        Branch branch = Session.get( SessionItem.BRANCH ) as Branch
        orderPanel = new OrderPanel()
        mainPanel.add( 'orderPanel', orderPanel )
        mainPanel.layout.show( mainPanel, 'orderPanel' )

        userLabel.text = "[${user?.username ?: ''}] ${user?.fullName ?: ''}"
        branchLabel.text = "[${branch?.id ?: ''}] ${branch?.name ?: ''}"
        versionLabel.text = version
        infoBar.visible = true

    }

    private void initialize( ) {
        String fechaParametro = Registry.fechaPrimerArranque
        String fechaActual = fecha.format(new Date())
        if(fechaParametro != ''){
          if(!fechaActual.trim().equalsIgnoreCase(fechaParametro)){
            IOController.getInstance().deletCustomerProcess()
            IOController.getInstance().updateInitialDate(fechaActual)
          }
        } else {
            IOController.getInstance().updateInitialDate(fechaActual)
        }
        sb.doOutside {
            IOController.getInstance().autoUpdateFxRates()
            PriceListController.loadExpiredPriceList()
            DailyCloseController.openDay()
            DailyCloseController.RegistrarPromociones()
            IOController.getInstance().autoUpdateEmployeeFile()
            IOController.getInstance().startAsyncNotifyDispatcher()
        }
    }

    void requestImportPartMaster( ) {
        IOController.getInstance().requestImportPartMaster()
    }

    void requestImportPartClass( ) {
        IOController.getInstance().requestImportClasificationArtMaster()
    }

    void generateInventoryFile( ){
        ItemController.generateInventoryFile()
    }

    JPanel getMainPanel( ) {
        return mainPanel
    }

    void entrega(){
        EntregaTrabajoDialog trabajo = new EntregaTrabajoDialog(this)
        trabajo.show()
    }

    void requestNewSalesDay( ) {
        OpenSalesController.instance.requestNewDay()
    }

    void requestLogout( ) {
        AccessController.logOut()
        infoBar.visible = false
        mainPanel.remove( orderPanel )
        mainPanel.remove( showOrderPanel )
        mainPanel.remove( dailyClosePanel )
        mainPanel.remove( priceListPanel )
        mainPanel.layout.show( mainPanel, 'logInPanel' )
    }

    static void main( args ) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext( "classpath:spring-config.xml" )
        ctx.registerShutdownHook()
        SwingUtilities.invokeLater(
                new Runnable() {
                    void run( ) {
                        version = Session.getVersion()
                        MainWindow window = new MainWindow()
                        window.initialize()
                        window.show()
                    }
                }
        )
    }
}
