package mx.lux.pos.ui.view.component

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.view.driver.PromotionDriver

import javax.swing.*
import java.awt.event.MouseEvent

class DiscountContextMenu extends JPopupMenu {

  private SwingBuilder sb = new SwingBuilder( )
  private PromotionDriver driver
  private JMenuItem menuDiscount
  private JMenuItem menuCorporateDiscount
  private JMenuItem menuCouponDiscount

  
  DiscountContextMenu( PromotionDriver pDriver ) {
    driver = pDriver
    buildUI( )
  }
  
  protected buildUI( ) {
    sb.popupMenu( this ) {
      menuDiscount = menuItem( text: "Descuento Tienda",
        visible: true,
        actionPerformed: { onDiscountSelected( ) },
      )
      menuCorporateDiscount = menuItem( text: "Descuento Corporativo", 
        visible: true,
        actionPerformed: { onCorporateDiscountSelected( ) },
      )
        menuCouponDiscount = menuItem( text: "Descuento por Cupon",
                visible: true,
                actionPerformed: { onCouponDiscountSelected( ) },
        )
    }
  }
  
  // Public Methods
  void activate( MouseEvent pEvent ) {
    menuDiscount.setEnabled( driver.isDiscountEnabled( ) )
    menuCorporateDiscount.setEnabled( driver.isCorporateDiscountEnabled( ) )
    menuCouponDiscount.setEnabled(driver.isCorporateDiscountEnabled())
      show( pEvent.getComponent(), pEvent.getX(), pEvent.getY() )
  } 
  
  // UI Response
  protected void onDiscountSelected( ) {
    driver.requestDiscount( )
  }
  
  protected void onCorporateDiscountSelected( ) {
    driver.requestCorporateDiscount( )
  }

  protected void onCouponDiscountSelected(){
      driver.requestCouponDiscount()
  }


  
}
