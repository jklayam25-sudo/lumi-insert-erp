package lumi.insert.app.service.implement;
 
import java.time.LocalDateTime; 
import java.time.temporal.ChronoUnit; 
import java.util.UUID;
 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.AuthToken;
import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.repository.AuthTokenRepository;
import lumi.insert.app.core.repository.EmployeeRepository;
import lumi.insert.app.dto.request.AuthTokenCreateRequest;
import lumi.insert.app.dto.response.AuthTokenResponse;
import lumi.insert.app.exception.AuthenticationTokenException;
import lumi.insert.app.mapper.AuthMapper;
import lumi.insert.app.service.AuthTokenService;
import lumi.insert.app.utils.security.JwtUtils;

@Service
@Transactional
@Slf4j
public class AuthTokenServiceImpl implements AuthTokenService{

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    AuthTokenRepository authTokenRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    AuthMapper authMapper;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    @ActivityLogger(
        entityName = "auth_tokens",
        action = ActivityAction.LOGIN_SUCCESS,
        actionMessage = "Employee login success"
    )
    public AuthTokenResponse createAuthToken(AuthTokenCreateRequest request) {
        log.info("Authenticating employee username={}", request.getUsername());
        Employee employee = employeeRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.debug("Login failed, username not found={}", request.getUsername());
                return new AuthenticationTokenException("Employee with username " + request.getUsername() + " is not found");
            });
        
        if(!employee.isActive()) {
            log.debug("Login failed, employee is not active, username={}", request.getUsername());
            throw new AccountExpiredException("Employee with username " + request.getUsername() + " is not active");
        }

        if(!(passwordEncoder.matches(request.getPassword(), employee.getPassword()))) {
            log.debug("Login failed, password mismatch for username={}", request.getUsername());
            throw new BadCredentialsException("Bad credentials, wrong password!");
        }

        String accessToken = jwtUtils.getAccessToken(employee);

        authTokenRepository.deleteByEmployeeId(employee.getId());

        AuthToken authToken = AuthToken.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .employee(employee)
            .refreshToken(UUID.randomUUID().toString())
            .expiredAt(LocalDateTime.now().plus(7, ChronoUnit.DAYS))
            .build();

        AuthToken savedToken = authTokenRepository.save(authToken);
        log.info("Login succeeded for employeeId={}, username={}", employee.getId(), request.getUsername());
        return authMapper.createDtoResponseFromEntity(accessToken, savedToken);
    }

    public AuthTokenResponse refreshAuthToken(String refreshToken) {
        log.info("Refreshing auth token for refreshToken={}", refreshToken);
        AuthToken authToken = authTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> {
                log.debug("Refresh failed, token not found={}", refreshToken);
                return new AuthenticationTokenException("Credentials token is not valid");
            });
        
        if(authToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            log.debug("Refresh token expired for authTokenId={}, employeeId={}", authToken.getId(), authToken.getEmployee().getId());
            authTokenRepository.delete(authToken);
            throw new AuthenticationTokenException("Credentials token is expired");
        }

        Employee employee = authToken.getEmployee();

        String accessToken = jwtUtils.getAccessToken(employee);

        log.info("Refresh succeeded for employeeId={}, refreshToken={}", employee.getId(), refreshToken);
        return authMapper.createDtoResponseFromEntity(accessToken, authToken);
    }

    @Override
    @ActivityLogger(
        entityName = "auth_tokens",
        action = ActivityAction.LOGOUT,
        actionMessage = "Employee logout"
    )
    public void deleteRefreshToken(String refreshToken) {
        log.info("Deleting refresh token={}", refreshToken);
        authTokenRepository.deleteByRefreshToken(refreshToken);
    }
    
}
