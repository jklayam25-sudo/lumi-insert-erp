package lumi.insert.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Request to create a transaction payment")
public class TransactionPaymentCreateRequest {
    
    @NotNull(message = "paymentFrom must not be null")
    @Schema(description = "Payment method source (e.g., CASH, BANK_TRANSFER)", example = "CASH")
    String paymentFrom;

    @NotNull(message = "paymentTo must not be null")
    @Schema(description = "Payment destination account/method", example = "REGISTER_1")
    String paymentTo;

    @NotNull(message = "totalPayment must not be null")
    @Min(value = 1, message = "totalPayment minimal value is 1")
    @Schema(description = "Payment amount", example = "100000")
    Long totalPayment;

    @Schema(description = "File(image) to be store")
    @NotNull(message = "files must not be null")
    MultipartFile[] files;

}
