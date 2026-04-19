package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.EmployeePicture;

/**
 * Repository for {@link EmployeePicture} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface EmployeePictureRepository extends JpaRepository<EmployeePicture, UUID>{
    
}
