package lumi.insert.app.controller.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.core.entity.Product;
import lumi.insert.app.dto.request.ProductUpdateRequest;
import lumi.insert.app.dto.response.ProductDeleteResponse;
import lumi.insert.app.dto.response.ProductResponse;
import lumi.insert.app.exception.BoilerplateRequestException;
import lumi.insert.app.utils.forTesting.ProductUtils;

public class ProductControllerUpdateTest extends BaseProductControllerTest{

    @Test
    @DisplayName("Should response with updated entity")
    public void updateProductAPI_shouldReturnOKWithUpdatedEntity() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.updateProduct(any(ProductUpdateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            put("/api/products/1")
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
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.name").value("Product"))
        .andExpect(jsonPath("$.data.category.name").value("Category"));
    }

    @Test
    @DisplayName("Should response with updated entity")
    @WithMockUser(username = "admin", roles = "OWNER")
    public void updateProductAPI_higherRole_shouldReturnOKWithUpdatedEntity() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.updateProduct(any(ProductUpdateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            put("/api/products/1")
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
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.name").value("Product"))
        .andExpect(jsonPath("$.data.category.name").value("Category"));
    }

    @WithMockUser(username = "admin", roles = "FINANCE")
    @Test
    @DisplayName("Should response with updated entity") 
    public void updateProductAPI_invalidRole_shouldReturnOKWithUpdatedEntity() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.updateProduct(any(ProductUpdateRequest.class))).thenReturn(dtoResponseFromProduct);

        mockMvc.perform(
            put("/api/products/1")
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
    @DisplayName("Should response with MethodArgsTypeMissMatch due to wrong type input")
    public void updateProductAPI_shouldThrownTypeMissExc() throws Exception{
        mockMvc.perform(
            put("/api/products/were")
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
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("id must be Long"));
    }

    @Test
    @DisplayName("Should response with MethodArgsTypeMissMatch due to wrong type input")
    public void activateProductAPI_shouldThrownTypeMissExc() throws Exception{
        mockMvc.perform(
            post("/api/products/were/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should response with MethodArgsTypeMissMatch due to wrong type input")
    public void deactivateProductAPI_shouldThrownTypeMissExc() throws Exception{
        mockMvc.perform(
            post("/api/products/were/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should response with OK Status and response with field isActive false")
    public void deactivateProductAPI_shouldReturnOK() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        mockCategorizedProduct.setIsActive(false);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.deactivateProduct(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(false))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with OK Status and response with field isActive false")
    @WithMockUser(username = "admin", roles = "OWNER")
    public void deactivateProductAPI_higherRole_shouldReturnOK() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        mockCategorizedProduct.setIsActive(false);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.deactivateProduct(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(false))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with OK Status and response with field isActive true")
    @WithMockUser(username = "admin", roles = "OWNER")
    public void activateProductAPI_higherRole_shouldReturnOK() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        mockCategorizedProduct.setIsActive(true);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.activateProduct(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products/1/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with OK Status and response with field isActive true")
    public void activateProductAPI_shouldReturnOK() throws Exception{
        Product mockCategorizedProduct = ProductUtils.getMockCategorizedProduct();
        mockCategorizedProduct.setIsActive(true);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(mockCategorizedProduct);

        when(productService.activateProduct(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/products/1/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with BoilerplateRequest due to no change gonna happen even if method run")
    public void deactivateProductAPI_shouldThrownBoilerplateExc() throws Exception{
        when(productService.deactivateProduct(1L)).thenThrow(new BoilerplateRequestException("Product with ID 1 already inactive"));
        mockMvc.perform(
            post("/api/products/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isNotImplemented()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Product with ID 1 already inactive"));

    }
}
