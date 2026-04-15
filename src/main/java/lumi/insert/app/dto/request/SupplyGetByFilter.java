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
import lumi.insert.app.core.entity.nondatabase.SupplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Schema(description = "Filter request for searching supply orders")
public class SupplyGetByFilter extends PaginationRequest{

    @Builder.Default
    @Min(value = 0, message = "minTotalItems minimal value is 0")
    @Schema(description = "Minimum number of supply items", example = "1")
    BigDecimal minTotalItems = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalItems minimal value is 0")
    @Schema(description = "Maximum number of supply items", example = "200")
    BigDecimal maxTotalItems = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minGrandTotal minimal value is 0")
    @Schema(description = "Minimum supply grand total", example = "500000")
    BigDecimal minGrandTotal = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxGrandTotal minimal value is 0")
    @Schema(description = "Maximum supply grand total", example = "100000000")
    BigDecimal maxGrandTotal = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minTotalUnpaid minimal value is 0")
    @Schema(description = "Minimum unpaid supply amount", example = "0")
    BigDecimal minTotalUnpaid = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalUnpaid minimal value is 0")
    @Schema(description = "Maximum unpaid supply amount", example = "20000000")
    BigDecimal maxTotalUnpaid = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minTotalPaid minimal value is 0")
    @Schema(description = "Minimum paid supply amount", example = "0")
    BigDecimal minTotalPaid = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxTotalPaid minimal value is 0")
    @Schema(description = "Maximum paid supply amount", example = "100000000")
    BigDecimal maxTotalPaid = BigDecimal.valueOf(9999999999990L);

    @Schema(description = "Filter by supply status", example = "COMPLETED")
    SupplyStatus status;

    @Schema(description = "Filter by supplier ID")
    UUID supplierId;

    @Schema(description = "Start date for supply range", example = "2024-01-01T00:00:00")
    LocalDateTime minCreatedAt;

    @Schema(description = "End date for supply range", example = "2024-12-31T23:59:59")
    LocalDateTime maxCreatedAt;

    @Builder.Default
    @Pattern(regexp = "createdAt|updatedAt|totalItems|totalFee|totalDiscount|subTotal|grandTotal|totalUnpaid|totalPaid|totalUnrefunded|totalRefunded", message = "check documentation for sortBy specification")
    String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "DESC|ASC", message = "check documentation for sortDirection specification")
    String sortDirection = "DESC";

}
