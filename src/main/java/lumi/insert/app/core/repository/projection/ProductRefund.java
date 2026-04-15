package lumi.insert.app.core.repository.projection;

import java.math.BigDecimal;

public record ProductRefund (
    String productName,
    BigDecimal totalRefunded
) {}
