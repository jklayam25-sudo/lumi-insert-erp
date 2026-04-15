package lumi.insert.app.dto.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor 
@Schema(description = "Request to create a new supply order")
public class SupplyCreateRequest {

    @NotNull(message = "supplierId cannot be empty")
    @Schema(description = "Supplier ID for this order", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID supplierId;

    @NotNull(message = "invoiceId cannot be empty")
    @Schema(description = "Invoice or order number from supplier", example = "INV-2024-001")
    private String invoiceId;

    @Size(min = 1, message = "items cannot be empty")
    @Builder.Default
    @Valid
    @Schema(description = "List of items in the supply order")
    private List<SupplyItemCreate> supplyItems = new ArrayList<>();

    @Schema(description = "Order description or notes", example = "Regular monthly supply")
    private String description;

    @NotNull(message = "totalFee cannot be empty")
    @Min(value = 0, message = "totalFee cannot below 0")
    @Schema(description = "Additional fees/charges", example = "50000")
    private BigDecimal totalFee;

    @NotNull(message = "totalDiscount cannot be empty")
    @Min(value = 0, message = "totalDiscount cannot below 0")
    @Schema(description = "Total discount amount", example = "10000")
    private BigDecimal totalDiscount;
 
}
