package lumi.insert.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus; 

@Schema(description = "Full detail response for a transaction, including itemized product list") 
@Builder
public record TransactionDetailResponse(
    
    @Schema(description = "Primary key of the transaction", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id, 
    
    @Schema(description = "Generated invoice number", example = "INV-2024-001")
    String invoiceId, 
    
    @Schema(description = "ID of the customer who made the purchase", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    UUID customerId, 

    @Schema(description = "customer identity name", example = "Creative Leno Inc.")
    String customerName,
    
    @ArraySchema(schema = @Schema(description = "List of all products and quantities received in this batch"))
    List<TransactionItemResponse> transactionItems, 

    @Schema(description = "Number of unique items in the transaction", example = "5")
    Long totalItems, 
    
    @Schema(description = "Additional service fees applied", example = "5000")
    Long totalFee, 
    
    @Schema(description = "Total discount amount applied", example = "10000")
    Long totalDiscount, 
    
    @Schema(description = "Amount before tax and fees", example = "100000")
    Long subTotal, 
    
    @Schema(description = "Final amount to be paid by the customer", example = "95000")
    Long grandTotal, 
    
    @Schema(description = "Remaining balance that hasn't been paid", example = "0")
    Long totalUnpaid, 
    
    @Schema(description = "Total amount already paid by the customer", example = "95000")
    Long totalPaid, 
    
    @Schema(description = "Remaining balance eligible for refund", example = "95000")
    Long totalUnrefunded, 
    
    @Schema(description = "Total amount already refunded to the customer", example = "0")
    Long totalRefunded, 
    
    @Schema(description = "Current state of the transaction", example = "PAID")
    TransactionStatus status, 
    
    @Schema(description = "ID of the staff/cashier who handled the transaction", example = "b2c1d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
    UUID staffId, 
    
    @ArraySchema(schema = @Schema(description = "System or audit messages related to the transaction", example = "Payment received via QRIS"))
    List<String> messages, 
    
    @Schema(description = "Timestamp when the transaction was created")
    LocalDateTime createdAt, 
    
    @Schema(description = "Timestamp when the transaction was last updated")
    LocalDateTime updatedAt
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}