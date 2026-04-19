package lumi.insert.app.core.entity.nondatabase;

import lumi.insert.app.core.entity.StockCard;

/**
 * Used to identify Product movement from {@link StockCard}. 
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public enum StockMove {
    PURCHASE,
    CUSTOMER_IN,
    CUSTOMER_OUT,
    SUPPLIER_IN,
    SUPPLIER_OUT,
    SALE,
    DEFECT,
    REPAIRED
}
