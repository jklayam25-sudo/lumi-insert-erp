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

/**
 * Application scheduler & CRONJOB.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Component
@Slf4j
public class LumiScheduler {

    @Autowired
    MailSenderService mailSenderService;

    private EmployeeLogin springScheduler = new EmployeeLogin(null, "springScheduler", null, "0.0.0.0");
    
    /**
     * Daily task: export and send products stats to email
     * Time: 00:03 everyday
     */
    @Scheduled(cron = "0 3 0 * * *", zone = "Asia/Jakarta") 
    void dailyProductsStatistics(){
        log.info("Starting daily products statistics email generation");
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(springScheduler, null, null));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endDate = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999);
            log.debug("Daily statistics date range: {} to {}", startDate, endDate);
            mailSenderService.sendProductsStatistic(startDate, endDate);
            log.info("Daily products statistics email sent successfully");
        } catch (Exception e) {
            log.error("Fail to send product statistics, message: " + e.getMessage());
        }
    }

    /**
     * Monthly task: export and send products stats to email
     * Time: 01:05 every first day of month
     */
    @Scheduled(cron = "0 5 1 1 * *", zone = "Asia/Jakarta")
    void monthlyProductsStatistics(){
        log.info("Starting monthly products statistics email generation");
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(springScheduler, null, null));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endDate = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999);
            log.debug("Monthly statistics date range: {} to {}", startDate, endDate);
            mailSenderService.sendProductsStatistic(startDate, endDate);
            log.info("Monthly products statistics email sent successfully");
        } catch (Exception e) {
            log.error("Fail to send product statistics, message: " + e.getMessage());
        }
    }

}
