package lumi.insert.app.controller.stockcard;

import java.math.BigDecimal;
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
 import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class BaseStockCardControllerTest extends BaseControllerTest{
     
    StockCardResponse stockCardResponse = new StockCardResponse(UUID.randomUUID(), UUID.randomUUID(), 1L, "Product", BigDecimal.valueOf(-5L), BigDecimal.valueOf(10L), BigDecimal.valueOf(5L), BigDecimal.valueOf(1000L), BigDecimal.valueOf(1000L), StockMove.CUSTOMER_OUT, null, LocalDateTime.now());

    List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("OWNER");

    EmployeeLogin employeeLogin = EmployeeLogin.builder()
        .id(UUID.randomUUID())
        .username("lumi")
        .role(EmployeeRole.OWNER)
        .build();

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, roles);
 
}
