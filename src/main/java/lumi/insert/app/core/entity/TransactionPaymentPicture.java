package lumi.insert.app.core.entity;

import java.util.UUID;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;

@Entity(name = "transaction_payment_pics")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data 
@Audited
public class TransactionPaymentPicture extends BaseAuditing{
    
    @Id 
    private UUID id;

    private String pictureUrl;

    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_payment_id", nullable = false)
    @NotAudited
    private TransactionPayment transactionPayment;

}
