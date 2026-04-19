package lumi.insert.app.core.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lumi.insert.app.core.entity.AuthToken;

/**
 * Repository for {@link AuthToken} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    
    Optional<AuthToken> findByRefreshToken(String token);

    void deleteByRefreshToken(String token);

    void deleteByEmployeeId(UUID employeeId);
    
}
