package lumi.insert.app.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed profile of a supplier including contact information and financial summary")
public record SupplierDetailResponse(
    
    @Schema(description = "Primary key of the supplier record", example = "c0a80101-8b7c-1d2e-9f0a-112233445566")
    UUID id, 
    
    @Schema(description = "Full legal or trade name of the supplier", example = "PT. Nusantara Perkasa")
    String name, 
    
    @Schema(description = "Official business email address", example = "sales@nusantaraperkasa.com")
    String email, 
    
    @Schema(description = "Primary phone number or PIC contact", example = "+62-812-3456-7890")
    String contact, 
    
    @Schema(description = "Lifetime count of supply transactions with this vendor", example = "152")
    Long totalTransaction, 
    
    @Schema(description = "Total amount currently owed to this supplier (Accounts Payable)", example = "4500000")
    BigDecimal totalUnpaid, 
    
    @Schema(description = "Total lifetime amount paid to this supplier", example = "125000000")
    BigDecimal totalPaid, 
    
    @Schema(description = "Status indicating if the supplier is currently enabled for new orders", example = "true")
    Boolean isActive
    
)  implements Identifiable {

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

}