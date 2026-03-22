package lumi.insert.app.controller.category;

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

import lumi.insert.app.core.entity.Category; 
import lumi.insert.app.dto.request.CategoryUpdateRequest;
import lumi.insert.app.dto.response.CategoryResponse;
import lumi.insert.app.exception.BoilerplateRequestException;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.utils.forTesting.CategoryUtils;

public class CategoryControllerUpdateTest extends BaseCategoryControllerTest {

    @Test
    @DisplayName("Should return 200 http status and response of updated DTO")
    public void editCategoryAPI_validRequest_return200StatusAndResponseDTO() throws Exception{
        Category mockCategory = CategoryUtils.getMockCategory();
        CategoryResponse dtoResponseFromCategory = categoryMapper.createDtoResponseFromCategory(mockCategory);

        when(categoryService.updateCategoryName(any(CategoryUpdateRequest.class))).thenReturn(dtoResponseFromCategory);

         mockMvc.perform(
            put("/api/categories/1")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Category") 
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.name").value("Category"));
    }
 
    @Test
    @DisplayName("Should return 403 when authority role not warehouse or higher")
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void editCategoryAPI_invalidAuth_returnForbidden() throws Exception{ 
         mockMvc.perform(
            put("/api/categories/1")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Category") 
        )
        .andDo(print())
        .andExpect(status().isForbidden()) 
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"))
        .andExpect(jsonPath("$.data ").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 http status when request parameter type is not valid")
    public void editCategoryAPI_invalidIdRequest_return400() throws Exception{
 
         mockMvc.perform(
            put("/api/categories/were")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Category") 
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 http status when requested entity is not found")
    public void editCategoryAPI_notFound_return404() throws Exception{ 
        when(categoryService.updateCategoryName(any())).thenThrow(new NotFoundEntityException("Category with ID " + 1 + " was not found"));
        mockMvc.perform(
            put("/api/categories/1")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Category") 
        )
        .andDo(print())
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 409 http status when request name already exists")
    public void editCategoryAPI_duplicateName_return409() throws Exception{ 
        when(categoryService.updateCategoryName(any())).thenThrow(new DuplicateEntityException("Category with Name Category already exists"));
        mockMvc.perform(
            put("/api/categories/1")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Category") 
        )
        .andDo(print())
        .andExpect(status().isConflict()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 400 http status when request parameter type is not valid")
    public void activateCategoryAPI_invalidIdRequest_return400() throws Exception{
        mockMvc.perform(
            post("/api/categories/were/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 403 when authority role not warehouse or higher")
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void activateCategoryAPI_invalidAuth_returnForbidden() throws Exception{ 
         mockMvc.perform(
            post("/api/categories/1/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isForbidden()) 
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"))
        .andExpect(jsonPath("$.data ").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 http status when request parameter type is not valid")
    public void deactivateCategoryAPI_invalidIdRequest_return400() throws Exception{
        mockMvc.perform(
            post("/api/categories/were/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 403 when authority role not warehouse or higher")
    @WithMockUser(username = "admin", roles = "FINANCE")
    public void deactivateCategoryAPI_invalidAuth_returnForbidden() throws Exception{ 
         mockMvc.perform(
            post("/api/categories/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isForbidden()) 
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"))
        .andExpect(jsonPath("$.data ").isEmpty());
    }

    @Test
    @DisplayName("Should response with 200 OK Status and response with field isActive false")
    public void deactivateCategoryAPI_validRequest_shouldReturnOK() throws Exception{
        Category mockCategory = CategoryUtils.getMockCategory();
        mockCategory.setIsActive(false);

        CategoryResponse deleteDtoResponseFromProduct = categoryMapper.createDtoResponseFromCategory(mockCategory);

        when(categoryService.deactivateCategory(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/categories/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(false))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with 200 OK Status and response with field isActive true")
    public void activateCategoryAPI_validRequest_shouldReturnOK() throws Exception{
        Category mockCategory = CategoryUtils.getMockCategory();
        mockCategory.setIsActive(true);

        CategoryResponse deleteDtoResponseFromProduct = categoryMapper.createDtoResponseFromCategory(mockCategory);

        when(categoryService.activateCategory(1L)).thenReturn(deleteDtoResponseFromProduct);

        mockMvc.perform(
            post("/api/categories/1/activate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk()) 
        .andExpect(jsonPath("$.data.isActive").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should response with BoilerplateRequest due to no change gonna happen even if method run")
    public void deactivateCategoryAPI_categoryAlreadyInactive_shouldThrownBoilerplateExc() throws Exception{
        when(categoryService.deactivateCategory(1L)).thenThrow(new BoilerplateRequestException("Category with ID 1 already inactive"));
        mockMvc.perform(
            post("/api/categories/1/deactivate")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isNotImplemented()) 
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Category with ID 1 already inactive"));

    }
}
