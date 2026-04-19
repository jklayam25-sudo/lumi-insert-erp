package lumi.insert.app.utils.generator;

import java.time.LocalDateTime; 
import org.springframework.stereotype.Component;

/**
 * Utilities of Date's related.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Component
public class DateUtils {
    
    public LocalDateTime getFirstDateThisMonth(){
        LocalDateTime now = LocalDateTime.now();
        return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public LocalDateTime getFirstDateNextMonth(){
        LocalDateTime now = LocalDateTime.now();
        return now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public LocalDateTime getFirstDateLastMonth(){
        LocalDateTime now = LocalDateTime.now();
        return now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
}
