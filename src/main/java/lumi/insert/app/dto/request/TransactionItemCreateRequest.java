package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to add an item to a transaction")
public class TransactionItemCreateRequest {
    
    @NotNull(message =  "productId must not be null")
    @Schema(description = "Product ID to add to transaction", example = "1")
    Long productId;

    @NotNull(message =  "quantity must not be null")
    @Min(message = "quantity minimal value is 1", value = 1)
    @Schema(description = "Quantity of the product", example = "5")
    BigDecimal quantity;

}
