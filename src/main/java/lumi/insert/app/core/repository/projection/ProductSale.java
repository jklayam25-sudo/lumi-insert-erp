package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

/**
 * Represent sales product informations.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record ProductSale (
    String productName,
    BigDecimal totalSold
) {}
