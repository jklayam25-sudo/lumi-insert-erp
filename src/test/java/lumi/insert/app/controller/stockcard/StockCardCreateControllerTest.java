package lumi.insert.app.controller.stockcard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import lumi.insert.app.dto.request.StockCardCreateRequest;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

public class StockCardCreateControllerTest extends BaseStockCardControllerTest{
    
    @Test
    void createStockCardAPI_validRequest_returnCreatedAndDTO() throws Exception{
        when(stockCardService.createStockCard(any(StockCardCreateRequest.class))).thenReturn(stockCardResponse);

        mockMvc.perform(
            post("/api/stockcards")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("referenceId", UUID.randomUUID().toString())
            .param("productId", "1")
            .param("quantity", "-5")
            .param("type", "CUSTOMER_OUT")
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.productId").value(stockCardResponse.productId()))
        .andExpect(jsonPath("$.data.id").value(stockCardResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(stockCardService, times(1)).createStockCard(argThat(arg -> arg.getQuantity().compareTo(BigDecimal.valueOf(-5L)) == 0 && arg.getType().equals("CUSTOMER_OUT")));
    }

    @Test
    void createStockCardAPI_incompleteParam_returnBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/stockcards")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("referenceId", UUID.randomUUID().toString()) 
            .param("quantity", "-5")
            .param("type", "CUSTOMER_OUT")
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("productId cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void createStockCardAPI_missmatchType_returnBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/stockcards")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("referenceId", UUID.randomUUID().toString()) 
            .param("productId", "1")
            .param("quantity", "-5")
            .param("type", "CUSTOMER-XX")
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("check documentation for type"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void createStockCardAPI_invalidRequest_returnUnprocessable() throws Exception{
        when(stockCardService.createStockCard(any(StockCardCreateRequest.class))).thenThrow(new TransactionValidationException("Stock 'IN' type should be positive quantity"));

        mockMvc.perform(
            post("/api/stockcards")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("referenceId", UUID.randomUUID().toString())
            .param("productId", "1")
            .param("quantity", "-15")
            .param("type", "CUSTOMER_IN")
        )
        .andDo(print())
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors").value("Stock 'IN' type should be positive quantity"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void createStockCardAPI_notFoundProductOrReference_returnCreatedAndDTO() throws Exception{
        when(stockCardService.createStockCard(any(StockCardCreateRequest.class))).thenThrow(new NotFoundEntityException("Transaction Items with ID " + stockCardResponse.referenceId() + " was not found"));

        mockMvc.perform(
            post("/api/stockcards")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("referenceId", UUID.randomUUID().toString())
            .param("productId", "1")
            .param("quantity", "-15")
            .param("type", "CUSTOMER_IN")
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Transaction Items with ID " + stockCardResponse.referenceId() + " was not found"))
        .andExpect(jsonPath("$.data").isEmpty());
    }
}
