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
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Schema(description = "Filter request for searching transactions")
public class TransactionGetByFilter extends PaginationRequest{

    @Builder.Default
    @Min(value = 0, message = "minTotalItems minimal value is 0")
    @Schema(description = "Minimum number of transaction items", example = "0")
    Long minTotalItems = 0L;

    @Builder.Default
    @Min(value = 0, message = "maxTotalItems minimal value is 0")
    @Schema(description = "Maximum number of transaction items", example = "100")
    Long maxTotalItems = 9999990L;

    @Builder.Default
    @Min(value = 0, message = "minGrandTotal minimal value is 0")
    @Schema(description = "Minimum transaction grand total", example = "100000")
    BigDecimal minGrandTotal = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxGrandTotal minimal value is 0")
    @Schema(description = "Maximum transaction grand total", example = "50000000")
    BigDecimal maxGrandTotal = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minTotalUnpaid minimal value is 0")
    @Schema(description = "Minimum unpaid amount", example = "0")
    BigDecimal minTotalUnpaid = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalUnpaid minimal value is 0")
    @Schema(description = "Maximum unpaid amount", example = "10000000")
    BigDecimal maxTotalUnpaid = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minTotalPaid minimal value is 0")
    @Schema(description = "Minimum paid amount", example = "0")
    BigDecimal minTotalPaid = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalPaid minimal value is 0")
    @Schema(description = "Maximum paid amount", example = "50000000")
    BigDecimal maxTotalPaid = BigDecimal.valueOf(9999999999990L);

    @Schema(description = "Filter by transaction status", example = "COMPLETED")
    TransactionStatus status;

    @Schema(description = "Filter by customer ID")
    UUID customerId;

    @Schema(description = "Start date for transaction range", example = "2024-01-01T00:00:00")
    LocalDateTime minCreatedAt;

    @Schema(description = "End date for transaction range", example = "2024-12-31T23:59:59")
    LocalDateTime maxCreatedAt;

    @Builder.Default
    @Pattern(regexp = "createdAt|updatedAt|totalItems|totalFee|totalDiscount|subTotal|grandTotal|totalUnpaid|totalPaid|totalUnrefunded|totalRefunded", message = "check documentation for sortBy specification")
    String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "DESC|ASC", message = "check documentation for sortDirection specification")
    String sortDirection = "DESC";

}
