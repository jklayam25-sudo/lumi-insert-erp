package lumi.insert.app.core.entity.nondatabase;

/**
 * Used to sort Transaction query. 
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public enum TransactionSortOrder {
    createdAt,
    updatedAt,
    totalItems,
    totalFee,
    totalDiscount,
    subTotal,
    grandTotal,
    totalUnpaid,
    totalPaid,
    totalUnrefunded,
    totalRefunded;
}
