package lumi.insert.app.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object representing an individual item received from a supplier")
public record SupplyItemResponse(
    
    @Schema(description = "Unique identifier of the supply item record", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    UUID id, 
    
    @Schema(description = "Nested object containing the product details (Name, SKU, etc.)")
    ProductName product, 
    
    @Schema(description = "Unit cost price charged by the supplier for this specific delivery", example = "12500")
    BigDecimal price, 
    
    @Schema(description = "Quantity of the product received from the supplier", example = "100")
    BigDecimal quantity, 
    
    @Schema(description = "Additional notes or specifications for this batch (e.g., Expiry Date, Batch No)", example = "Batch #A2024 - Exp: 2026-12")
    String description
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}