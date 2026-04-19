package lumi.insert.app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configuration class to define Role-Based Access Control (RBAC) inheritance.
 * This allows higher-privileged roles to automatically possess the authorities
 * granted to lower-privileged roles.
 *  @author KelvinKhodes
 *  @since 1.0.0 
 */
@Configuration
public class RoleHierarchyConfig {
    
    /**
     * Defines the relationship between roles.
     * The syntax "ROLE_A > ROLE_B" means that anyone with ROLE_A 
     * effectively has all the permissions of ROLE_B.
     * * Hierarchy defined:
     * - OWNER has all permissions of FINANCE, WAREHOUSE, and CASHIER.
     */
    @Bean
    static RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_OWNER > ROLE_FINANCE
                ROLE_OWNER > ROLE_WAREHOUSE
                ROLE_OWNER > ROLE_CASHIER 
                """);
    }

    /**
     * Registers the RoleHierarchy with Spring Method Security.
     * This ensures that annotations like @PreAuthorize("hasRole('CASHIER')") 
     * will correctly return 'true' if the current user has ROLE_OWNER.
     * * @param roleHierarchy The hierarchy bean defined above.
     * @return A configured MethodSecurityExpressionHandler.
     */
    @SuppressWarnings("deprecation")
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
