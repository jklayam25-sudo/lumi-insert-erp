package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.CustomerPicture;

/**
 * Repository for {@link CustomerPicture} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface CustomerPictureRepository extends JpaRepository<CustomerPicture, UUID>{
    
}
