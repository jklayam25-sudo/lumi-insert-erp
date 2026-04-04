package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lumi.insert.app.core.entity.SupplyPaymentPicture;

public interface SupplyPaymentPictureRepository extends JpaRepository<SupplyPaymentPicture, UUID>{
    
}
