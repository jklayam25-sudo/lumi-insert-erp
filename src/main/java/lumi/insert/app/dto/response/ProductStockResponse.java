package lumi.insert.app.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Lightweight response containing only the current stock level of a product")
public record ProductStockResponse (
    
    @Schema(description = "Primary key of the product", example = "1001")
    Long id, 
    
    @Schema(description = "The current number of units available in the warehouse/store", example = "42")
    BigDecimal stockQuantity
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}