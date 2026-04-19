package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

/**
 * Represent refunded product  informations.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record ProductRefund (
    String productName,
    BigDecimal totalRefunded
) {}
