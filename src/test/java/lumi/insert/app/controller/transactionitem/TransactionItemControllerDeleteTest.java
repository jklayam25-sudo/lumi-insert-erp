package lumi.insert.app.controller.transactionitem;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lumi.insert.app.dto.response.TransactionItemDelete;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;

public class TransactionItemControllerDeleteTest extends BaseTransactionItemControllerTest {
    
    @Test
    @DisplayName("should return delete dto when request Trx item deleted")
    public void deleteTransactionItemAPI_validId_shouldReturnDeleteDTO() throws Exception{ 
        TransactionItemDelete transactionItemDelete = new TransactionItemDelete(transactionItemResponse.id(), UUID.randomUUID(), 1L, true, null);
        when(transactionItemService.deleteTransactionItem(transactionItemResponse.id())).thenReturn(transactionItemDelete);

        mockMvc.perform(
            delete("/api/transactions/" + UUID.randomUUID().toString() + "/items/" + transactionItemResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print()) 
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.data.id").value(transactionItemResponse.id().toString()))
        .andExpect(jsonPath("$.data.deleted").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void deleteTransactionItemAPI_higherRole_shouldReturnDeleteDTO() throws Exception{ 
        TransactionItemDelete transactionItemDelete = new TransactionItemDelete(transactionItemResponse.id(), UUID.randomUUID(), 1L, true, null);
        when(transactionItemService.deleteTransactionItem(transactionItemResponse.id())).thenReturn(transactionItemDelete);

        mockMvc.perform(
            delete("/api/transactions/" + UUID.randomUUID().toString() + "/items/" + transactionItemResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print()) 
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.data.id").value(transactionItemResponse.id().toString()))
        .andExpect(jsonPath("$.data.deleted").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void deleteTransactionItemAPI_invalidRole_shouldReturnDeleteDTO() throws Exception{ 
        TransactionItemDelete transactionItemDelete = new TransactionItemDelete(transactionItemResponse.id(), UUID.randomUUID(), 1L, true, null);
        when(transactionItemService.deleteTransactionItem(transactionItemResponse.id())).thenReturn(transactionItemDelete);

        mockMvc.perform(
            delete("/api/transactions/" + UUID.randomUUID().toString() + "/items/" + transactionItemResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }

    @Test
    @DisplayName("should return error not found when request Trx item Not Found")
    public void deleteTransactionItemAPI_invalidId_shouldReturnErrorNotFound() throws Exception{ 
        when(transactionItemService.deleteTransactionItem(transactionItemResponse.id())).thenThrow(new NotFoundEntityException("Transaction Items with ID " + 1L + " was not found"));
        
        mockMvc.perform(
            delete("/api/transactions/" + UUID.randomUUID().toString() + "/items/" + transactionItemResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print()) 
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.errors").value("Transaction Items with ID " + 1L + " was not found"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return error forbiddenrequest when request Transaction is not pending")
    public void deleteTransactionItemAPI_nonPendingTrx_shouldReturnErrorForbiddenRequest() throws Exception{ 
        when(transactionItemService.deleteTransactionItem(transactionItemResponse.id())).thenThrow(new ForbiddenRequestException("Couldn't delete the item because Transaction Status is not PENDING(CART)"));
        
        mockMvc.perform(
            delete("/api/transactions/" + UUID.randomUUID().toString() + "/items/" + transactionItemResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.errors").value("Couldn't delete the item because Transaction Status is not PENDING(CART)"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

}
