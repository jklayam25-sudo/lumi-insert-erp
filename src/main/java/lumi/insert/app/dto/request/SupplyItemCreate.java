package lumi.insert.app.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor 
public class SupplyItemCreate {
    
    @NotNull(message = "productId cannot be empty")
    @Schema(description = "Product ID to add to transaction", example = "1")
    private Long productId;

    @NotNull(message = "price cannot be empty")
    @Min(value = 0, message = "price cannot below 0")
    @Schema(description = "Base price from supplier", example = "500")
    private BigDecimal price;

    @NotNull(message = "quantity cannot be empty")
    @Min(value = 0, message = "quantity cannot below 0")
    @Schema(description = "Quantity of the product", example = "5")
    private BigDecimal quantity;

    private String description;
}
