package lumi.insert.app.worker; 

import java.time.LocalDateTime; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin; 
import lumi.insert.app.service.MailSenderService; 

@Component
@Slf4j
public class LumiScheduler {

    @Autowired
    MailSenderService mailSenderService;

    private EmployeeLogin springScheduler = new EmployeeLogin(null, "springScheduler", null, "0.0.0.0");
    
    @Scheduled(cron = "0 3 0 * * *", zone = "Asia/Jakarta") 
    void dailyProductsStatistics(){
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(springScheduler, null, null));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endDate = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999);
            mailSenderService.sendProductsStatistic(startDate, endDate);   
        } catch (Exception e) {
            log.error("Fail to send product statistics, message: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 5 1 * * *", zone = "Asia/Jakarta")
    void monthlyProductsStatistics(){
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(springScheduler, null, null));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endDate = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999);
            mailSenderService.sendProductsStatistic(startDate, endDate);    
        } catch (Exception e) {
            log.error("Fail to send product statistics, message: " + e.getMessage());
        }
    }

}
