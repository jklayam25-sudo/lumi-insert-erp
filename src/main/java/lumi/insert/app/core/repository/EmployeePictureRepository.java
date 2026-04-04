package lumi.insert.app.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lumi.insert.app.core.entity.EmployeePicture;

public interface EmployeePictureRepository extends JpaRepository<EmployeePicture, UUID>{
    
}
