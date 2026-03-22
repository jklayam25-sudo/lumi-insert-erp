package lumi.insert.app.controller.transactionpayment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.dto.request.TransactionPaymentCreateRequest;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;

public class TransactionPaymentControllerCreateTest extends BaseTransactionPaymentControllerTest{
    
    @Test
    @DisplayName("should return Transaction Payment Response when create succesfully")
    public void createTransactionPaymentAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        when(transactionPaymentService.createTransactionPayment(transactionPaymentResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(transactionPaymentResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionPaymentResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).createTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void createTransactionPaymentAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionPaymentService.createTransactionPayment(transactionPaymentResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(transactionPaymentResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionPaymentResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).createTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void createTransactionPaymentAPI_anyRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionPaymentService.createTransactionPayment(transactionPaymentResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(transactionPaymentResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionPaymentResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).createTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @DisplayName("should return error of notfound Response when request transaction notfound")
    public void createTransactionPaymentAPI_notFound_shouldReturnError() throws Exception{
        when(transactionPaymentService.createTransactionPayment(transactionPaymentResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenThrow(new NotFoundEntityException("Transaction with ID " + 1L + " was not found"));

        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error of tranasction validation Response when request transaction payment have left balance")
    public void createTransactionPaymentAPI_paymentExceeds_shouldReturnError() throws Exception{
        when(transactionPaymentService.createTransactionPayment(transactionPaymentResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenThrow(new TransactionValidationException("Payment exceeds the remaining transaction debts with ID " + 1L + ", enter an exact amount to proceed"));

        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isUnprocessableContent()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.errors").value("Payment exceeds the remaining transaction debts with ID " + 1L + ", enter an exact amount to proceed")); 
    }


    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void createTransactionPaymentAPI_incompleteParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI")  
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error of methodargs Response when totalpayment < 1")
    public void createTransactionPaymentAPI_minTotalPayment0_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI")  
            .param("totalPayment", "0")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void createTransactionPaymentAPI_missMatchParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "true")  
            .param("totalPayment", "true")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

 
    @Test
    @DisplayName("should return Transaction Payment Response when create succesfully")
    public void refundTransactionPaymentAPI_validRequest_shouldReturnCreatedEntity() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(transactionRefundResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionRefundResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionRefundResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.data.isForRefund").value(true)) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void refundTransactionPaymentAPI_higherRole_shouldReturnCreatedEntity() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(transactionRefundResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionRefundResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionRefundResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.data.isForRefund").value(true)) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void refundTransactionPaymentAPI_anyRole_shouldReturnCreatedEntity() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(transactionRefundResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionRefundResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionRefundResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.data.isForRefund").value(true)) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void refundTransactionPaymentAPI_incompleteParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI")  
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void refundTransactionPaymentAPI_minTotalPayment0_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI")  
            .param("totalPayment", "0")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void refundTransactionPaymentAPI_missMatchParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + transactionPaymentResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "true")  
            .param("totalPayment", "true")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty( )) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should return error notFound when entity transaction not found")
    public void refundTransactionPaymentAPI_notFound_shouldReturErrorNotFound() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new NotFoundEntityException("Transaction with ID " + 1 + " was not found"));

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Transaction with ID " + 1 + " was not found"));
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

     @Test
    @DisplayName("should return error Forbidden when trx status is PENDING/COMPLETE ")
    public void refundTransactionPaymentAPI_pendingTrx_shouldReturnErrorForbidden() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new ForbiddenRequestException("Refund payment only to Transaction with status PROCESS(onGoing) or CANCELLED, check carefully"));

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Refund payment only to Transaction with status PROCESS(onGoing) or CANCELLED, check carefully"));
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

     @Test
    @DisplayName("should return error transaction validation when amount refund bigger than transaction unrefunded")
    public void refundTransactionPaymentAPI_amountRefundExceed_shouldReturnErrorTransactionValidation() throws Exception{ 
        when(transactionPaymentService.refundTransactionPayment(transactionRefundResponse.transactionId(), TransactionPaymentCreateRequest.builder().totalPayment(transactionRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new TransactionValidationException("Payment refund exceeds the remaining transaction unrefunded debt with ID " + 1L + ", enter an exact amount to proceed"));

        mockMvc.perform(
            post("/api/transactions/" + transactionRefundResponse.transactionId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Payment refund exceeds the remaining transaction unrefunded debt with ID " + 1L + ", enter an exact amount to proceed"));
        verify(transactionPaymentService, times(1)).refundTransactionPayment(any(UUID.class), any(TransactionPaymentCreateRequest.class));
    }

}
