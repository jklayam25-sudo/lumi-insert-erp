package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

/**
 * Represent product stocks.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public interface ProductStockProjection {
    BigDecimal getStockQuantity();
}
