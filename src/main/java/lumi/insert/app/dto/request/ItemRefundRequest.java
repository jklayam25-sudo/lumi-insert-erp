package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to refund items from a transaction or supply")
public class ItemRefundRequest {
    
    @NotNull(message = "productId cannot be empty")
    @Schema(description = "Product ID to refund", example = "1")
    private Long productId;

    @NotNull(message = "quantity cannot be empty")
    @Min(value = 1, message = "quantity cannot below 1")
    @Schema(description = "Quantity to refund", example = "5")
    private BigDecimal quantity;

    @Schema(description = "Refund reason or description", example = "Damaged product")
    private String description;
}
