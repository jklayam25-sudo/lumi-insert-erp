package lumi.insert.app.service;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import lumi.insert.app.dto.request.ItemRefundRequest;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.TransactionItemCreateRequest;
import lumi.insert.app.dto.response.TransactionItemDelete;
import lumi.insert.app.dto.response.TransactionItemResponse;
import lumi.insert.app.dto.response.TransactionItemStatisticResponse; 

public interface TransactionItemService {
    
    TransactionItemResponse createTransactionItem(UUID transactionId, TransactionItemCreateRequest request);

    TransactionItemDelete deleteTransactionItem(UUID id);

    TransactionItemResponse updateTransactionItemQuantity(UUID id, BigDecimal quantity);

    Slice<TransactionItemResponse> getTransactionItemsByTransactionId(UUID transactionId, PaginationRequest paginationRequest);

    Slice<TransactionItemResponse> getTransactionByTransactionIdAndProductId(UUID transactionId, Long ProductId);

    TransactionItemResponse refundTransactionItem(UUID id, ItemRefundRequest request);

    TransactionItemResponse getTransactionItem(UUID id);

    TransactionItemStatisticResponse getTransactionItemStats(LocalDateTime startDate, LocalDateTime endDate);

}
