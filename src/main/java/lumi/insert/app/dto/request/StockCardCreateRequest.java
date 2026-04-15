package lumi.insert.app.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
  
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to create a new stock card record")
public class StockCardCreateRequest {
    
    @NotNull(message = "referenceId cannot be empty")
    @Schema(description = "Reference transaction/supply ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID referenceId;
 
    @NotNull(message = "productId cannot be empty")
    @Schema(description = "Product ID for stock movement", example = "1")
    private Long productId; 
 
    @NotNull(message = "quantity cannot be empty and value atleas 1")
    @Schema(description = "Quantity of stock movement", example = "10")
    private BigDecimal quantity; 
 
    @NotBlank(message = "type cannot be empty")
    @Pattern(regexp = "CUSTOMER_IN|CUSTOMER_OUT|DEFECT|REPAIRED|SUPPLIER_IN|SUPPLIER_OUT|", message = "check documentation for type")
    @Schema(description = "Type of stock movement", example = "SUPPLIER_IN")
    private String type;

    @Schema(description = "Description of stock movement", example = "Received from supplier")
    private String description;

}
