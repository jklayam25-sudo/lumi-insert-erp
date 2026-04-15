package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

public record ProductRefreshProjection(
    Long id, 
    BigDecimal sellPrice, 
    BigDecimal stockQuantity) {
}
