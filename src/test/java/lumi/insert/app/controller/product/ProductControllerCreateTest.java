package lumi.insert.app.controller.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.core.entity.Product;
import lumi.insert.app.dto.request.ProductCreateRequest;
import lumi.insert.app.dto.response.ProductResponse;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.utils.forTesting.ProductUtils;

public class ProductControllerCreateTest extends BaseProductControllerTest{

    @Test
    public void createProductAPI_shouldReturnCreatedEntity() throws Exception{
        Product mockProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockProduct);

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isCreated()) 
        .andExpect(jsonPath("$.data.name").value("Product"))
        .andExpect(jsonPath("$.data.category.name").value("Category"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void createProductAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        Product mockProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockProduct);

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isCreated()) 
        .andExpect(jsonPath("$.data.name").value("Product"))
        .andExpect(jsonPath("$.data.category.name").value("Category"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void createProductAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        Product mockProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockProduct);

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    public void createProductAPI_shouldThrownDuplicateEntityExc() throws Exception{
         when(productService.createProduct(any())).thenThrow(new DuplicateEntityException("Product with name Product already exists"));

         mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isConflict()) 
        .andExpect(jsonPath("$.errors").value("Product with name Product already exists"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should response with NotFoundError due to invalid category id")
    public void createProductAPI_shouldThrownNotFoundExc() throws Exception{
         when(productService.createProduct(any())).thenThrow(new NotFoundEntityException("Category with ID 1 was not found"));

         mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.errors").value("Category with ID 1 was not found"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should response with MethodArgsInvalidExc (NotNull) due to Null Param")
    public void createProductAPI_shouldThrownMethodArgsExc() throws Exception{
         mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "")
            .param("basePrice", "1000")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should response with MethodArgsInvalidExc (Min) due to Min Validation")
    public void createProductAPI_shouldThrownMethodArgsExc2() throws Exception{
         mockMvc.perform(
            post("/api/products")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Product")
            .param("basePrice", "-1")
            .param("sellPrice", "1200")
            .param("stockQuantity", "10")
            .param("stockMinimum", "1")
            .param("categoryId", "1")
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }
}
