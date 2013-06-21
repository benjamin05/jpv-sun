package mx.lux.pos.ui.model

interface CustomerListener {

  void reset( )

  void setCustomer( Customer pCustomer )

  void setOperationTypeSelected( OperationType pOperation )

  void setOrder( Order pOrder )

  void disableUI()

  void enableUI()
}