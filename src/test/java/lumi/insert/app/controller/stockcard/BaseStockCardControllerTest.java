package lumi.insert.app.controller.stockcard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils; 

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.dto.response.StockCardResponse; 
 
public abstract class BaseStockCardControllerTest extends BaseControllerTest{
     
    StockCardResponse stockCardResponse = new StockCardResponse(UUID.randomUUID(), UUID.randomUUID(), 1L, "Product", -5L, 10L, 5L, 1000L, 1000L, StockMove.CUSTOMER_OUT, null, LocalDateTime.now());

    List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("OWNER");

    EmployeeLogin employeeLogin = EmployeeLogin.builder()
        .id(UUID.randomUUID())
        .username("lumi")
        .role(EmployeeRole.OWNER)
        .build();

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, roles);
 
}
