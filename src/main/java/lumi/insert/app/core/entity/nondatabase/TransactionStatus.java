package lumi.insert.app.core.entity.nondatabase;

import lumi.insert.app.core.entity.Transaction;

/**
 * Represent status of {@link Transaction}. 
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public enum TransactionStatus {
    PENDING,
    PROCESS,
    COMPLETE,
    CANCELLED
}
