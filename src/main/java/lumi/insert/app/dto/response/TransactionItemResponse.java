package lumi.insert.app.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object representing an individual item within a transaction")
public record TransactionItemResponse(
    
    @Schema(description = "Unique identifier of the transaction item", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    UUID id, 
    
    @Schema(description = "ID of the parent transaction", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID transactionId, 
    
    @Schema(description = "ID of the product associated with this item", example = "1001")
    Long productId, 

    @Schema(description = "Product name", example = "Espresso Arabica 250g")
    String productName, 
    
    @Schema(description = "additional details", example = "Wrong colour")
    String description, 
    
    @Schema(description = "Unit price of the product at the time of transaction", example = "35000")
    BigDecimal price, 
    
    @Schema(description = "Quantity of the product purchased", example = "2")
    BigDecimal quantity, 
    
    @Schema(description = "Timestamp when the item was added to the transaction")
    LocalDateTime createdAt, 
    
    @Schema(description = "Timestamp when the item was last updated")
    LocalDateTime updatedAt
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}