package lumi.insert.app.worker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.mail.MessagingException;
import lumi.insert.app.service.MailSenderService;

@ExtendWith(MockitoExtension.class)
public class LumiSchedulerTest {
    
    @InjectMocks
    LumiScheduler lumiScheduler;

    @Mock
    MailSenderService mailSenderService;

    @Test
    void productStatistics_shouldCallMailSenderService() throws MessagingException{
        lumiScheduler.dailyProductsStatistics();
        verify(mailSenderService, times(1)).sendProductsStatistic(argThat(arg -> arg.getDayOfMonth() == LocalDateTime.now().minusDays(1).getDayOfMonth()), any());
    }

    @Test
    void productStatistics_monthly_shouldCallMailSenderService() throws MessagingException{
        lumiScheduler.monthlyProductsStatistics();
        verify(mailSenderService, times(1)).sendProductsStatistic(argThat(arg -> arg.getDayOfMonth() == 1), any());
    }
}
