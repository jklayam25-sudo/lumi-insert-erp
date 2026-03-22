package lumi.insert.app.controller.supplypayment;

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

import lumi.insert.app.dto.request.SupplyPaymentCreateRequest;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException; 

public class SupplyPaymentControllerCreateTest extends BaseSupplyPaymentControllerTest{
    
    @Test
    @DisplayName("should return Supply Payment Response when create succesfully")
    public void createSupplyPaymentAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        when(supplyPaymentService.createSupplyPayment(supplyPaymentResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(supplyPaymentResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplyPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.supplyId").value(supplyPaymentResponse.supplyId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(supplyPaymentService, times(1)).createSupplyPayment(any(UUID.class), any(SupplyPaymentCreateRequest.class));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER")
    public void createSupplyPaymentAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(supplyPaymentService.createSupplyPayment(supplyPaymentResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(supplyPaymentResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplyPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.supplyId").value(supplyPaymentResponse.supplyId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void createSupplyPaymentAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        when(supplyPaymentService.createSupplyPayment(supplyPaymentResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenReturn(supplyPaymentResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "CLIENT")
            .param("paymentTo", "LUMI") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }

    @Test 
    public void createSupplyPaymentAPI_notFound_shouldReturnError() throws Exception{
        when(supplyPaymentService.createSupplyPayment(supplyPaymentResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenThrow(new NotFoundEntityException("Supply with ID " + 1L + " was not found"));

        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
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
    @DisplayName("should return error of tranasction validation Response when request supply payment have left balance")
    public void createSupplyPaymentAPI_paymentExceeds_shouldReturnError() throws Exception{
        when(supplyPaymentService.createSupplyPayment(supplyPaymentResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyPaymentResponse.totalPayment()).paymentFrom("CLIENT").paymentTo("LUMI").build())).thenThrow(new TransactionValidationException("Payment exceeds the remaining supply debts with ID " + 1L + ", enter an exact amount to proceed"));

        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
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
        .andExpect(jsonPath("$.errors").value("Payment exceeds the remaining supply debts with ID " + 1L + ", enter an exact amount to proceed")); 
    }


    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void createSupplyPaymentAPI_incompleteParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
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
    public void createSupplyPaymentAPI_minTotalPayment0_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
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
    public void createSupplyPaymentAPI_missMatchParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
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
    @DisplayName("should return Supply Payment Response when create succesfully")
    public void refundSupplyPaymentAPI_validRequest_shouldReturnCreatedEntity() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(supplyRefundResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplyRefundResponse.id().toString()))
        .andExpect(jsonPath("$.data.supplyId").value(supplyRefundResponse.supplyId().toString())) 
        .andExpect(jsonPath("$.data.isForRefund").value(true)) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(supplyPaymentService, times(1)).refundSupplyPayment(any(UUID.class), any(SupplyPaymentCreateRequest.class));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER")
    public void refundSupplyPaymentAPI_higherRole_shouldReturnCreatedEntity() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(supplyRefundResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplyRefundResponse.id().toString()))
        .andExpect(jsonPath("$.data.supplyId").value(supplyRefundResponse.supplyId().toString())) 
        .andExpect(jsonPath("$.data.isForRefund").value(true)) 
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void refundSupplyPaymentAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenReturn(supplyRefundResponse);

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    @DisplayName("should return error of methodargs Response when request param incomplete/ null")
    public void refundSupplyPaymentAPI_incompleteParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/refund")
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
    public void refundSupplyPaymentAPI_minTotalPayment0_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/refund")
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
    public void refundSupplyPaymentAPI_missMatchParam_shouldReturnError() throws Exception{
        mockMvc.perform(
            post("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/refund")
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
    @DisplayName("should return error notFound when entity supply not found")
    public void refundSupplyPaymentAPI_notFound_shouldReturErrorNotFound() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new NotFoundEntityException("Supply with ID " + 1 + " was not found"));

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Supply with ID " + 1 + " was not found"));
        verify(supplyPaymentService, times(1)).refundSupplyPayment(any(UUID.class), any(SupplyPaymentCreateRequest.class));
    }

     @Test
    @DisplayName("should return error Forbidden when trx status is PENDING/COMPLETE ")
    public void refundSupplyPaymentAPI_pendingTrx_shouldReturnErrorForbidden() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new ForbiddenRequestException("Refund payment only to Supply with status PROCESS(onGoing) or CANCELLED, check carefully"));

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Refund payment only to Supply with status PROCESS(onGoing) or CANCELLED, check carefully"));
        verify(supplyPaymentService, times(1)).refundSupplyPayment(any(UUID.class), any(SupplyPaymentCreateRequest.class));
    }

     @Test
    @DisplayName("should return error supply validation when amount refund bigger than supply unrefunded")
    public void refundSupplyPaymentAPI_amountRefundExceed_shouldReturnErrorSupplyValidation() throws Exception{ 
        when(supplyPaymentService.refundSupplyPayment(supplyRefundResponse.supplyId(), SupplyPaymentCreateRequest.builder().totalPayment(supplyRefundResponse.totalPayment()).paymentFrom("LUMI").paymentTo("CLIENT").build())).thenThrow(new TransactionValidationException("Payment refund exceeds the remaining supply unrefunded debt with ID " + 1L + ", enter an exact amount to proceed"));

        mockMvc.perform(
            post("/api/supplies/" + supplyRefundResponse.supplyId() + "/payments/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("paymentFrom", "LUMI")
            .param("paymentTo", "CLIENT") 
            .param("totalPayment", "10000")
        )
        .andDo(print()) 
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Payment refund exceeds the remaining supply unrefunded debt with ID " + 1L + ", enter an exact amount to proceed"));
        verify(supplyPaymentService, times(1)).refundSupplyPayment(any(UUID.class), any(SupplyPaymentCreateRequest.class));
    }

}
