package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.SupplyPaymentPicture;

/**
 * Repository for {@link SupplyPaymentPicture} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface SupplyPaymentPictureRepository extends JpaRepository<SupplyPaymentPicture, UUID>{
    
}
