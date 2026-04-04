package lumi.insert.app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {
    
    @Bean
    static RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_OWNER > ROLE_FINANCE
                ROLE_OWNER > ROLE_WAREHOUSE
                ROLE_OWNER > ROLE_CASHIER 
                """);
    }

    @SuppressWarnings("deprecation")
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
