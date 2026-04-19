package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

/**
 * Represent product that considered as outOfStock (quantity lower than minimum limit).
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record ProductOutOfStock(
    Long id,
    String name,
    BigDecimal stockQuantity,
    BigDecimal stockMinimum
) {}
