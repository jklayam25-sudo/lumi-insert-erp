package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

/**
 * Represent lasted product informations.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record ProductRefreshProjection(
    Long id, 
    BigDecimal sellPrice, 
    BigDecimal stockQuantity) {
}
