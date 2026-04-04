package lumi.insert.app.controller.employee;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.dto.request.EmployeeCreateRequest;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class EmployeeControllerCreateTest extends BaseEmployeeControllerTest{
    
    @Test
    @DisplayName("should return employee entity with code created Response when create succesfully")
    public void createEmployeeAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        LocalDateTime now = LocalDateTime.now();
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
        .username(employeeResponse.username())
        .fullname(employeeResponse.fullname())
        .password("secret$")
        .joinDate(now)
        .build();

        when(employeeService.createEmployee(request)).thenReturn(employeeResponse);

        mockMvc.perform(
            post("/api/employees")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", employeeResponse.username())
            .param("fullname", employeeResponse.fullname()) 
            .param("password", "secret$") 
            .param("joinDate", now.toString())
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(employeeResponse.id().toString()))
        .andExpect(jsonPath("$.data.fullname").value(employeeResponse.fullname())) 
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(employeeService, times(1)).createEmployee(request);
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void createEmployeeAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{ 
        mockMvc.perform(
            post("/api/employees")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", employeeResponse.username())
            .param("fullname", employeeResponse.fullname()) 
            .param("password", "secret$") 
            .param("joinDate", LocalDateTime.now().toString())
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    @DisplayName("should return error validation with code badreq when password value doesn't meet pattern")
    public void createEmployeeAPI_illegalParam_shouldReturnError() throws Exception{
        LocalDateTime now = LocalDateTime.now();

        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
        .username(employeeResponse.username())
        .fullname(employeeResponse.fullname())
        .password("se$")
        .joinDate(now)
        .build();

        when(employeeService.createEmployee(request)).thenReturn(employeeResponse);

        mockMvc.perform(
            post("/api/employees")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", employeeResponse.username())
            .param("fullname", employeeResponse.fullname()) 
            .param("password", "stfwa  42$") 
            .param("joinDate", now.toString())
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())  
        .andExpect(jsonPath("$.errors").value("password has to be 5-50 length and has atleast 1 unique char"));
        verify(employeeService, times(0)).createEmployee(request);
    }

    @Test
    @DisplayName("should return error DuplicateEnt when username already exists")
    public void createEmployeeAPI_duplicateUsername_shouldReturnBadRequest() throws Exception{
        LocalDateTime now = LocalDateTime.now();

        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
        .username(employeeResponse.username())
        .fullname(employeeResponse.fullname())
        .password("secret$")
        .joinDate(now)
        .build();

        when(employeeService.createEmployee(request)).thenThrow(new DuplicateEntityException("Employee with username " + request.getUsername() + " already exists"));

        mockMvc.perform(
            post("/api/employees")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", employeeResponse.username())
            .param("fullname", employeeResponse.fullname()) 
            .param("password", "secret$") 
            .param("joinDate", now.toString())
        )
        .andDo(print()) 
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.data").isEmpty())  
        .andExpect(jsonPath("$.errors").value("Employee with username " + request.getUsername() + " already exists"));
        verify(employeeService, times(1)).createEmployee(request);
    }

    @Test 
    public void uploadEmployeeProfileAPI_validRequest_shouldReturnResult() throws Exception{  
        when(employeeService.setEmployeeProfile(any(), any())).thenReturn(true);

        mockMvc.perform(
            multipart("/api/employees/" + UuidCreator.getRandomBasedFast() + "/profile")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
            .file(mockMultipartFile)
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(true)); 
    }

    @Test 
    public void uploadEmployeeProfileAPI_filesBroken_shouldReturnBadRequest() throws Exception{   
        mockMvc.perform(
            multipart("/api/employees/" + UuidCreator.getRandomBasedFast() + "/profile")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
            .file(mockBroken)
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test 
    public void uploadEmployeeProfileAPI_filesBigSize_returnBadRequest() throws Exception{   
        mockMvc.perform(
            multipart("/api/employees/" + UuidCreator.getRandomBasedFast() + "/profile")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
            .file(mockBigSize)
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test 
    public void uploadEmployeeProfileAPI_filesNotImage_returnBadRequest() throws Exception{   
        mockMvc.perform(
            multipart("/api/employees/" + UuidCreator.getRandomBasedFast() + "/profile")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
            .file(mockNotImage)
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test 
    public void uploadEmployeeProfileAPI_notFound_returnNotFOund() throws Exception{ 
        when(employeeService.setEmployeeProfile(any(), any())).thenThrow(new NotFoundEntityException("Not Found"));  
        mockMvc.perform(
            multipart("/api/employees/" + UuidCreator.getRandomBasedFast() + "/profile")
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON_VALUE) 
            .file(mockMultipartFile)
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }
}
