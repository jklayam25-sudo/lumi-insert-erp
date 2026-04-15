package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min; 
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to update an existing product")
public class ProductUpdateRequest {
     
    @Schema(description = "Product ID to update", example = "1")
    private Long id;

    @Schema(description = "Updated product name", example = "Laptop")
    private String name; 

    @Min(value = 0, message = "basePrice cannot below 0")
    @Schema(description = "Updated base cost price", example = "500000")
    private BigDecimal basePrice;

    @Min(value = 0, message = "sellPrice cannot below 0")
    @Schema(description = "Updated selling price", example = "750000")
    private BigDecimal sellPrice;
  
    @Min(value = 0, message = "stockMinimum cannot below 0")
    @Schema(description = "Updated minimum stock level", example = "10")
    private BigDecimal stockMinimum;

    @Schema(description = "Updated category ID", example = "1")
    private Long categoryId;

}
