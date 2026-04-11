package lumi.insert.app.config.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;

@Component
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String>{

    @Override
    public Optional<String> getCurrentAuditor() {

       return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(auth -> {
            if (auth.getPrincipal() instanceof EmployeeLogin){
                return ((EmployeeLogin) auth.getPrincipal()).getUsername();
            }
            return auth.getPrincipal().toString();
        });

    }

    public Optional<String> getAuditorIpAddress(){ 
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(auth -> {
            if (auth.getPrincipal() instanceof EmployeeLogin){
                return ((EmployeeLogin) auth.getPrincipal()).getIpAddress();
            }
            return "0.0.0.0";
        });
        
    }
    
}
