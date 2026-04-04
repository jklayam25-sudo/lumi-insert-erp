package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lumi.insert.app.core.entity.TransactionPaymentPicture;

public interface TransactionPaymentPictureRepository extends JpaRepository<TransactionPaymentPicture, UUID>{
    
}
