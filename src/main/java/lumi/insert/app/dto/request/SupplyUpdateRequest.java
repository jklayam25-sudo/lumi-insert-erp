package lumi.insert.app.dto.request;
 
 
import jakarta.validation.constraints.Min;  
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to update an existing supply order")
public class SupplyUpdateRequest {
  
    @Schema(description = "Updated invoice or order number", example = "INV-2024-002")
    private String invoiceId;
 
    @Schema(description = "Updated order description", example = "Revised supply order")
    private String description;
 
    @Min(value = 1, message = "totalFee cannot below 0")
    @Schema(description = "Updated additional fees", example = "75000")
    private BigDecimal totalFee;

    @Min(value = 1, message = "totalDiscount cannot below 0")
    @Schema(description = "Updated discount amount", example = "15000")
    private BigDecimal totalDiscount;
 
}
