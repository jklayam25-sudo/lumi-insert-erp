package lumi.insert.app.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Schema(description = "Filter request for searching supply payments")
public class SupplyPaymentGetByFilter extends PaginationRequest{
 
    @Schema(description = "Filter by supply ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID supplyId;

    @Builder.Default
    @Min(value = 0, message = "minTotalPayment minimal value is 0")
    @Schema(description = "Minimum payment amount", example = "100000")
    BigDecimal minTotalPayment = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalPayment minimal value is 0")
    @Schema(description = "Maximum payment amount", example = "10000000")
    BigDecimal maxTotalPayment = BigDecimal.valueOf(9999999999990L);
 
    @Schema(description = "Start date for payment range", example = "2024-01-01T00:00:00")
    LocalDateTime minCreatedAt;

    @Schema(description = "End date for payment range", example = "2024-12-31T23:59:59")
    LocalDateTime maxCreatedAt;

    @Builder.Default
    @Pattern(regexp = "createdAt|updatedAt|totalPayment", message = "check documentation for sortBy specification")
    String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "DESC|ASC", message = "check documentation for sortDirection specification")
    String sortDirection = "DESC";

}
