package lumi.insert.app.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lumi.insert.app.core.entity.nondatabase.SupplyStatus; 

@Schema(description = "Response object containing detailed summary of a supply/procurement transaction")
public record SupplyResponse(
    
    @Schema(description = "Primary key of the supply record", example = "7d2e410b-58cc-4372-a567-0e02b2c3d479")
    UUID id, 
    
    @Schema(description = "Supplier's invoice or reference number", example = "SUPP-INV-88291")
    String invoiceId, 
    
    @Schema(description = "ID of the supplier company", example = "c0a80101-8b7c-1d2e-9f0a-112233445566")
    UUID supplierId, 

    @Schema(description = "supplier identity name", example = "Creative Leno Inc.")
    String supplierName,
    
    @Schema(description = "Total number of items received in this supply", example = "50")
    Long totalItems, 
    
    @Schema(description = "Additional shipping or handling fees from supplier", example = "25000")
    BigDecimal totalFee, 
    
    @Schema(description = "Discount granted by the supplier", example = "50000")
    BigDecimal totalDiscount, 
    
    @Schema(description = "Total amount before discounts and fees", example = "2000000")
    BigDecimal subTotal, 
    
    @Schema(description = "Final amount to be paid to the supplier", example = "1975000")
    BigDecimal grandTotal, 
    
    @Schema(description = "Debt amount still owed to the supplier", example = "975000")
    BigDecimal totalUnpaid, 
    
    @Schema(description = "Amount already paid to the supplier", example = "1000000")
    BigDecimal totalPaid, 
    
    @Schema(description = "Remaining balance that can still be claimed as a refund/return", example = "1000000")
    BigDecimal totalUnrefunded, 
    
    @Schema(description = "Total amount already refunded/returned by the supplier", example = "0")
    BigDecimal totalRefunded, 
    
    @Schema(description = "Current procurement status (e.g., RECEIVED, PENDING, PAID)", example = "RECEIVED")
    SupplyStatus status, 
    
    @Schema(description = "ID of the staff member who received the supply", example = "b2c1d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
    UUID staffId, 
    
    @ArraySchema(schema = @Schema(description = "Notes or audit logs related to this supply", example = "Checked by warehouse manager"))
    List<String> messages, 
    
    @Schema(description = "Timestamp when the supply was recorded")
    LocalDateTime createdAt
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}