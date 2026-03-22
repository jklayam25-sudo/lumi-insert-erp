package lumi.insert.app.controller.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.dto.request.EmployeeUpdateRequest;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class EmployeeControllerUpdateTest extends BaseEmployeeControllerTest{
    
    @Test
    @DisplayName("should return employee entity with Ok Response when RESET SUCCEED")
    public void resetEmployeePasswordAPI_validRequest_shouldReturnEntity() throws Exception{ 
        when(employeeService.resetEmployeePassword(employeeResponse.id(), "newPW%")).thenReturn(employeeResponse);

        mockMvc.perform(
            post("/api/employees/" + employeeResponse.id() + "/reset") 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("password", "newPW%")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(employeeResponse.id().toString()))
        .andExpect(jsonPath("$.data.fullname").value(employeeResponse.fullname())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(employeeService, times(1)).resetEmployeePassword(any(), argThat(arg -> arg.equals("newPW%")));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void resetEmployeePasswordAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{ 
        mockMvc.perform(
            post("/api/employees/" + employeeResponse.id() + "/reset") 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("password", "newPW%")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    @DisplayName("should return bad request Response when new password doesn't meet pattern")
    public void resetEmployeePasswordAPI_wrongPattern_shouldReturnBadRequest() throws Exception{ 
        mockMvc.perform(
            post("/api/employees/" + employeeResponse.id() + "/reset") 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("password", "new%")
        )
        .andDo(print())
        .andExpect(status().isBadRequest()) 
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("password has to be 5-50 length and has atleast 1 unique char"));
        verify(employeeService, times(0)).resetEmployeePassword(any(), any());
    }

    @Test
    @DisplayName("should return not found when entity is not found")
    public void resetEmployeePasswordAPI_notFound_shouldReturnNotFound() throws Exception{
        when(employeeService.resetEmployeePassword(employeeResponse.id(), "newPW%")).thenThrow(new NotFoundEntityException("Employee with ID " + employeeResponse.id() + " was not found"));

        mockMvc.perform(
            post("/api/employees/" + employeeResponse.id() + "/reset") 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("password", "newPW%")
        )
        .andDo(print())
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.errors").value("Employee with ID " + employeeResponse.id() + " was not found")) 
        .andExpect(jsonPath("$.data").isEmpty());
        verify(employeeService, times(1)).resetEmployeePassword(any(), argThat(arg -> arg.equals("newPW%")));
    }

    @Test
    @DisplayName("should return employee entity with Ok Response when update entity success")
    public void updateEmployeeAPI_validRequest_shouldReturnEntity() throws Exception{ 
        EmployeeUpdateRequest request = EmployeeUpdateRequest.builder()
        .role("FINANCE")
        .build();

        when(employeeService.updateEmployee(employeeResponse.id(), request)).thenReturn(employeeResponse);

        mockMvc.perform(
            patch("/api/employees/" + employeeResponse.id()) 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("role", "FINANCE")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(employeeResponse.id().toString())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(employeeService, times(1)).updateEmployee(any(), argThat(arg -> arg.getRole().equals(EmployeeRole.FINANCE.toString()) && arg.getUsername() == null));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void updateEmployeeAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{ 
        mockMvc.perform(
            patch("/api/employees/" + employeeResponse.id()) 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("role", "FINANCE")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    @DisplayName("should return employee entity with Ok Response when update entity success")
    public void updateEmployeeAPI_invalidRole_shouldReturnEntity() throws Exception{  

        mockMvc.perform(
            patch("/api/employees/" + employeeResponse.id()) 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("role", "TYPO")
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("check documentation for role specification")); 
    }

    @Test
    @DisplayName("should return notFound response when entity not found")
    public void updateEmployeeAPI_notFound_shouldReturnNotFound() throws Exception{  
        when(employeeService.updateEmployee(eq(employeeResponse.id()), any())).thenThrow(new NotFoundEntityException("Employee with ID " + employeeResponse.id() + " was not found"));

        mockMvc.perform(
            patch("/api/employees/" + employeeResponse.id()) 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("role", "FINANCE")
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Employee with ID " + employeeResponse.id() + " was not found")); 
    }

    @Test
    @DisplayName("should return duplicateentity with conflict status when request username is exists")
    public void updateEmployeeAPI_duplicateUsername_shouldReturnConflict() throws Exception{  
        when(employeeService.updateEmployee(eq(employeeResponse.id()), any())).thenThrow(new DuplicateEntityException("Employee with username " + " already exists"));

        mockMvc.perform(
            patch("/api/employees/" + employeeResponse.id()) 
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("username", "testalready")
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.data").isEmpty()) 
        .andExpect(jsonPath("$.errors").value("Employee with username " + " already exists")); 
    }

}
