package lumi.insert.app.service.mailsender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.core.repository.projection.ProductOutOfStock;
import lumi.insert.app.dto.response.TransactionDetailResponse;
import lumi.insert.app.dto.response.TransactionItemStatisticResponse;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.service.PdfService;
import lumi.insert.app.service.ProductService;
import lumi.insert.app.service.TransactionItemService;
import lumi.insert.app.service.implement.MailSenderServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MailSenderServiceTest {

    @Mock
    private JavaMailSender sender;

    @Mock
    private PdfService pdfService;

    @Mock
    private ProductService productService;

    @Mock
    private TransactionItemService transactionItemService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AllTransactionMapper allTransactionMapper;

    @InjectMocks
    private MailSenderServiceImpl mailSenderService;

    @Mock
    private MimeMessage mimeMessage;

    private TransactionInvoiceMail request;
    private Transaction transaction;
    private TransactionDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        request = new TransactionInvoiceMail(UuidCreator.getRandomBasedFast(), "user@mail.com", null);
        transaction = new Transaction();
        detailResponse = TransactionDetailResponse.builder().invoiceId("INV-001").customerName("Customer A").build(); 

       
    }

    @Test
    void sendTransactionInvoice_Success() throws MessagingException {
        byte[] pdfContent = "pdf content".getBytes();
        ByteArrayInputStream pdfStream = new ByteArrayInputStream(pdfContent);

        when(transactionRepository.findByIdDetail(any())).thenReturn(Optional.of(transaction));
        when(allTransactionMapper.createTransactionDetailResponseDto(any())).thenReturn(detailResponse);
        when(pdfService.exportTransactionWithItems(any())).thenReturn(pdfStream);
        when(sender.createMimeMessage()).thenReturn(mimeMessage);
        mailSenderService.sendTransactionInvoice(request);

        verify(transactionRepository).findByIdDetail(request.transactionId());
        verify(pdfService).exportTransactionWithItems(detailResponse);
        verify(sender).send(any(MimeMessage.class));
    }

    @Test
    void sendTransactionInvoice_NotFound() {
        when(transactionRepository.findByIdDetail(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> {
            mailSenderService.sendTransactionInvoice(request);
        });

        verify(sender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendProductsStatistic_Success() throws MessagingException {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        TransactionItemStatisticResponse stats = TransactionItemStatisticResponse.builder().build();
        List<ProductOutOfStock> outOfStock = List.of(new ProductOutOfStock(1L, "Prod", BigDecimal.ZERO, BigDecimal.ZERO));
        byte[] pdfContent = "stat content".getBytes();
        ByteArrayInputStream pdfStream = new ByteArrayInputStream(pdfContent);

        when(transactionItemService.getTransactionItemStats(any(), any())).thenReturn(stats);
        when(productService.getOutOfStockProducts()).thenReturn(outOfStock);
        when(pdfService.exportProductsStatistic(any(), any(), any(), any())).thenReturn(pdfStream);
        when(sender.createMimeMessage()).thenReturn(mimeMessage);
        mailSenderService.sendProductsStatistic(start, end);

        verify(transactionItemService).getTransactionItemStats(start, end);
        verify(productService).getOutOfStockProducts();
        verify(pdfService).exportProductsStatistic(stats, outOfStock, start, end);
        verify(sender).send(any(MimeMessage.class));
    }
}

 