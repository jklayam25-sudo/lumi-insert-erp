package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a supply payment")
public class SupplyPaymentCreateRequest {
    
    @NotNull(message = "paymentFrom must not be null")
    @Schema(description = "Payment method source", example = "BANK")
    String paymentFrom;

    @NotNull(message = "paymentTo must not be null")
    @Schema(description = "Payment destination account", example = "SUPPLIER_ACC")
    String paymentTo;

    @NotNull(message = "totalPayment must not be null")
    @Min(value = 1, message = "totalPayment minimal value is 1")
    @Schema(description = "Payment amount", example = "500000")
    BigDecimal totalPayment;

    @NotNull(message = "files must not be null")
    @Schema(description = "File(image) to be store")
    MultipartFile[] files;

}
