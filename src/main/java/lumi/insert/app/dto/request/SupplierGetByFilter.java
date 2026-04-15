package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter request for searching suppliers")
public class SupplierGetByFilter extends PaginationRequest{

    @Pattern(regexp = "^[a-zA-Z0-9.-_ ]{5,30}$", message = "name has to be 5-30 length")
    @Schema(description = "Filter supplier by name", example = "ABC Trading Co")
    String name;

    @Email(message = "email doesn't meet the email format")
    @Schema(description = "Filter supplier by email", example = "admin@supplier.com")
    String email;

    @Schema(description = "Filter supplier by phone contact", example = "08567890123")
    String contact;

    @Schema(description = "Filter suppliers by active status", example = "true")
    Boolean isActive;

    @Builder.Default
    @Min(value = 0, message = "minTotalItems minimal value is 0")
    @Schema(description = "Minimum number of supplier transaction", example = "0")
    BigDecimal minTotalTransaction = BigDecimal.valueOf(0);

    @Builder.Default
    @Min(value = 0, message = "maxTotalItems minimal value is 0")
    @Schema(description = "Maximum number of supplier transaction", example = "150")
    BigDecimal maxTotalTransaction = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minGrandTotal minimal value is 0")
    @Schema(description = "Minimum unpaid amount to supplier", example = "0")
    BigDecimal minTotalUnpaid = BigDecimal.valueOf(0);

    @Builder.Default
    @Min(value = 0, message = "maxGrandTotal minimal value is 0")
    @Schema(description = "Maximum unpaid amount to supplier", example = "20000000")
    BigDecimal maxTotalUnpaid = BigDecimal.valueOf(9999999999990L);

    @Builder.Default
    @Min(value = 0, message = "minTotalUnpaid minimal value is 0")
    @Schema(description = "Minimum paid amount to supplier", example = "0")
    BigDecimal minTotalPaid = BigDecimal.valueOf(0);

    @Builder.Default
    @Min(value = 0, message = "maxTotalUnpaid minimal value is 0")
    @Schema(description = "Maximum paid amount to supplier", example = "100000000")
    BigDecimal maxTotalPaid = BigDecimal.valueOf(99999999990L);

    @Builder.Default
    @Pattern(regexp = "createdAt|updatedAt|totalTransaction|totalUnpaid|totalPaid|name", message = "check documentation for sortBy specification")
    String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "DESC|ASC", message = "check documentation for sortDirection specification")
    String sortDirection = "DESC";
}
