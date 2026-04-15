package lumi.insert.app.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
 
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lumi.insert.app.core.entity.nondatabase.StockMove;

@Builder
@Schema(description = "Response object representing a single entry in the stock ledger (Kartu Stok)")
public record StockCardResponse(
    
    @Schema(description = "Unique identifier of the stock card entry", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id, 
    
    @Schema(description = "ID of the related transaction or supply record that triggered this movement", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    UUID referenceId, 
    
    @Schema(description = "ID of the product being moved", example = "1001")
    Long productId, 
    
    @Schema(description = "Name of the product for quick display", example = "Espresso Arabica 250g")
    String productName, 
    
    @Schema(description = "The amount of stock changed in this movement", example = "10")
    BigDecimal quantity, 
    
    @Schema(description = "Stock level before this movement", example = "50")
    BigDecimal oldStock, 
    
    @Schema(description = "Stock level after this movement", example = "60")
    BigDecimal newStock, 
    
    @Schema(description = "Cost or selling price before this movement", example = "12000")
    BigDecimal oldPrice, 
    
    @Schema(description = "Cost or selling price applied to this movement", example = "12500")
    BigDecimal newPrice, 
    
    @Schema(description = "The nature of the movement (e.g., IN, OUT, ADJUSTMENT)", example = "IN")
    StockMove type, 
    
    @Schema(description = "Additional notes regarding this movement", example = "Received from Supplier INV-88291")
    String description, 
    
    @Schema(description = "Timestamp of when the stock movement occurred")
    LocalDateTime createdAt
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}