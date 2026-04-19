package lumi.insert.app.core.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.Employee;

/**
 * Repository for {@link Employee} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>{
    Optional<Employee> findByUsername(String username);

    boolean existsByUsername(String username);
}   
