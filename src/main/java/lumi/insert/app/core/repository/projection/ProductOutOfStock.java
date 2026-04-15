package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

public record ProductOutOfStock(
    Long id,
    String name,
    BigDecimal stockQuantity,
    BigDecimal stockMinimum
) {}
