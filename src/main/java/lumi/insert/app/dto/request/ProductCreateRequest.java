package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to create a new product")
public class ProductCreateRequest {

    @NotBlank(message = "Name cannot be empty")
    @Schema(description = "Product name", example = "Laptop")
    private String name;

    @NotNull(message = "basePrice cannot be empty")
    @Min(value = 0, message = "basePrice cannot below 0")
    @Schema(description = "Base cost price of the product", example = "500000")
    private BigDecimal basePrice;

    @NotNull(message = "sellPrice cannot be empty")
    @Min(value = 0, message = "sellPrice cannot below 0")
    @Schema(description = "Selling price of the product", example = "750000")
    private BigDecimal sellPrice;

    @NotNull(message = "stockQuantity cannot be empty")
    @Min(value = 0, message = "stockQuantity cannot below 0")
    @Schema(description = "Initial stock quantity", example = "100")
    private BigDecimal stockQuantity;

    @Schema(description = "Minimum stock level alerts", example = "10")
    private BigDecimal stockMinimum;

    @Schema(description = "Category ID to associate the product", example = "1")
    private Long categoryId;
}
