package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lumi.insert.app.core.entity.CustomerPicture;

public interface CustomerPictureRepository extends JpaRepository<CustomerPicture, UUID>{
    
}
