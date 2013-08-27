package mx.lux.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.ui.controller.OrderController
import org.apache.commons.lang3.math.NumberUtils

@Bindable
@ToString
@EqualsAndHashCode
class Order {

  private static final String OPHTALMIC_TYPE = 'B'

  String id
  String bill
  String comments
  String status
  Date date
  Branch branch
  Customer customer
  String employee
    String country
  List<OrderItem> items = [ ]
  List<Payment> payments = [ ]
  List<IPromotion> deals = [ ]
  BigDecimal total = BigDecimal.ZERO
  BigDecimal paid = BigDecimal.ZERO
  BigDecimal due = BigDecimal.ZERO
  String dioptra
  String udf2
  String udf3
  Integer rx

  private Double usdRate

  public Order( ) {
    this.usdRate = OrderController.requestUsdRate()
  }

  String getTicket( ) {
    "${branch?.costCenter ?: ''}-${bill ?: ''}"
  }

  private Double getUsdDue( ) {
    Double due = due.doubleValue() / this.usdRate
    return due
  }

  String getDueString( ) {
    String str = String.format( "\$%,.2f", this.due )
    if ( OrderController.requestUsdDisplayed() ) {
      str = String.format( "\$%,.2f/US\$%,.0f", this.due, this.usdDue )
    }
    return str
  }

  protected void round( ) {
    this.total = NumberUtils.createBigDecimal( String.format( "%.2f", this.total ) )
    this.paid = NumberUtils.createBigDecimal( String.format( "%.2f", this.paid ) )
  }

  static Order toOrder( NotaVenta notaVenta ) {
    if ( notaVenta?.id ) {
      Order order = new Order(
          id: notaVenta.id,
          bill: notaVenta.factura,
          date: notaVenta.fechaHoraFactura,
          comments: notaVenta.observacionesNv,
          status: notaVenta.sFactura,
          branch: Branch.toBranch( notaVenta.sucursal ),
          customer: Customer.toCustomer( notaVenta.cliente ),
          items: notaVenta.detalles?.collect {OrderItem.toOrderItem( it )},
          payments: notaVenta.pagos?.collect {Payment.toPaymment( it )},
          total: notaVenta.ventaNeta ?: 0,
          paid: notaVenta.sumaPagos ?: 0,
          country:notaVenta.udf2 != null ? notaVenta.udf2: '',
          employee: "${[notaVenta.idEmpleado]}${notaVenta.empleado?.nombreCompleto}",
          dioptra: notaVenta.codigo_lente,
          udf2: notaVenta.udf2,
          udf3: notaVenta.udf3,
          rx: notaVenta.receta
      )
      order.deals = new ArrayList<IPromotion>()
      if ( notaVenta.ordenPromDet != null ) {
        order.deals.addAll( notaVenta.ordenPromDet?.collect {OrderLinePromotion.toPromotions( it )} )
      }
      IPromotion descuento = OrderDiscount.toPromotions( notaVenta )
      if ( descuento != null ) {
        order.deals.add( descuento )
      }
      order.round()
      order.due = order.total.subtract( order.paid )
      order.usdRate = OrderController.requestUsdRate()
      return order
    }
    return null
  }

  Double getAdvancePct( ) {
    Double pct = 0
    if ( this.total.toDouble() < 0.001 ) {
      if ( this.paid.toDouble() > 0.001 ) {
        pct = 0.0
      } else {
        pct = 100.0
      }
    } else {
      pct = this.paid.toDouble() / this.total.toDouble()
    }
    return pct
  }

  Boolean containsOphtalmic( ) {
    Boolean contained = false
    for ( OrderItem orderLine : this.items ) {
      if ( orderLine.item.type.equals( OPHTALMIC_TYPE ) )  {
        contained = true
        break
      }
    }
    return contained
  }

}
