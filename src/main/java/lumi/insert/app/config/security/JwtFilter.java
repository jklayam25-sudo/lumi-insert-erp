package lumi.insert.app.config.security;

import java.io.IOException; 
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier; 
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.utils.security.JwtUtils;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter{

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    @Qualifier("handlerExceptionResolver") 
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {   
        String token = parseBearer(request);

        if(token == null){
            if(request.getServletPath().contains("/")) {
                doFilter(request, response, filterChain);
                return;
            }
            resolver.resolveException(request, response, null, new BadCredentialsException("Missing access token, try to request token!"));
        } 

        DecodedJWT accessToken; 

        try {
            accessToken = jwtUtils.parseAccessToken(token);    
        } catch (JWTVerificationException e) { 
            if(e instanceof TokenExpiredException){
                resolver.resolveException(request, response, null, new BadCredentialsException("Access token is expired, try to request token"));
                return;
            }
            resolver.resolveException(request, response, null, e);
            return;
        }
         
        List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("ROLE_" + accessToken.getClaim("role").asString());
 
        EmployeeLogin employeeLogin = EmployeeLogin.builder()
        .id(UUID.fromString(accessToken.getClaim("id").asString()))
        .username(accessToken.getClaim("username").asString())
        .role(EmployeeRole.valueOf(accessToken.getClaim("role").asString()))
        .ipAddress(request.getRemoteAddr())
        .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, roles);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private String parseBearer(HttpServletRequest request){
         String header = request.getHeader("Authorization");

         if(header == null) return null;
         String token = header.split("Bearer ")[1];;
         if(token.length() < 1) return null;
         return token;
    }

    
}
