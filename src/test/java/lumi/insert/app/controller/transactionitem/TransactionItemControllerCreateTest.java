package lumi.insert.app.controller.transactionitem;

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

import lumi.insert.app.dto.request.TransactionItemCreateRequest;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException; 

public class TransactionItemControllerCreateTest extends BaseTransactionItemControllerTest {
    
    @Test
    @DisplayName("should return Transaction Item Response when create succesfully")
    public void createTransactionItemAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        when(transactionItemService.createTransactionItem(transactionItemResponse.transactionId(), TransactionItemCreateRequest.builder().productId(1L).quantity(5L).build())).thenReturn(transactionItemResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")
            .param("quantity", "5") 
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionItemResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionItemResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionItemService, times(1)).createTransactionItem(any(UUID.class), any(TransactionItemCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void createTransactionItemAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionItemService.createTransactionItem(transactionItemResponse.transactionId(), TransactionItemCreateRequest.builder().productId(1L).quantity(5L).build())).thenReturn(transactionItemResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")
            .param("quantity", "5") 
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(transactionItemResponse.id().toString()))
        .andExpect(jsonPath("$.data.transactionId").value(transactionItemResponse.transactionId().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void createTransactionItemAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionItemService.createTransactionItem(transactionItemResponse.transactionId(), TransactionItemCreateRequest.builder().productId(1L).quantity(5L).build())).thenReturn(transactionItemResponse);

        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")
            .param("quantity", "5") 
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }
 
    @Test
    @DisplayName("should thrown method argument exception when id type missmatch")
    public void createTransactionItemAPI_missMatchId_shouldThrownBadRequest() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + true + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")
            .param("quantity", "5") 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should thrown method argument exception when param type missmatch")
    public void createTransactionItemAPI_missMatchParam_shouldThrownBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "true")
            .param("quantity", "5") 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should thrown NotNull exception when request param incomplete/missing")
    public void createTransactionItemAPI_incompleteRequest_shouldThrownBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "6") 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("should thrown Min exception when request param incomplete/missing")
    public void createTransactionItemAPI_quantity0_shouldThrownBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "6") 
            .param("quantity", "0") 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }
    
    @Test
    @DisplayName("should thrown NotFound exception when transaction not found")
    public void createTransactionItemAPI_notFoundTransaction_shouldThrownNotFound() throws Exception{ 
        when(transactionItemService.createTransactionItem(any(UUID.class), any())).thenThrow(new NotFoundEntityException("Transaction with ID 1 was not found"));
        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "6") 
            .param("quantity", "3") 
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").isNotEmpty());
        verify(transactionItemService, times(1)).createTransactionItem(any(UUID.class), any(TransactionItemCreateRequest.class));
    }

    @Test
    @DisplayName("should thrown TransactionValidation exception when product out of stock")
    public void createTransactionItemAPI_outOfStockProduct_shouldThrownNotFound() throws Exception{ 
        when(transactionItemService.createTransactionItem(any(UUID.class), any())).thenThrow(new TransactionValidationException("Product stocks with ID " + 1L + " doesn't meet buyer quantity, stock left: 0"));
        mockMvc.perform(
            post("/api/transactions/" + transactionItemResponse.transactionId() + "/items")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "6") 
            .param("quantity", "3") 
        )
        .andDo(print()) 
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Product stocks with ID " + 1L + " doesn't meet buyer quantity, stock left: 0"));
        verify(transactionItemService, times(1)).createTransactionItem(any(UUID.class), any(TransactionItemCreateRequest.class));
    }

    
    
}
