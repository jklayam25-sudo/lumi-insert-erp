package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

public record ProductSale (
    String productName,
    BigDecimal totalSold
) {}
