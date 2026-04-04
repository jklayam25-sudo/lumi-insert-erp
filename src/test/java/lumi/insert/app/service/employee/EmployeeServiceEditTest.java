package lumi.insert.app.service.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any; 
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
 
import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.dto.request.EmployeeUpdateRequest;
import lumi.insert.app.dto.response.EmployeeResponse;
import lumi.insert.app.exception.DatabaseInternalException;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.StorageActionException;

public class EmployeeServiceEditTest extends BaseEmployeeServiceTest{
    
    @Test
    @DisplayName("Should return employee DTO when saved successfully")
    void resetEmployeePassword_validRequest_returnEmployeeResponseDTO(){
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee));
        when(passwordEncoder.encode("newPassword")).thenReturn("decodedSECRET");
        when(employeeRepositoryMock.save(any(Employee.class))).thenAnswer(ans -> ans.getArgument(0));
        

        EmployeeResponse employeeDTO = employeeServiceMock.resetEmployeePassword(setupEmployee.getId(), "newPassword");
        assertEquals(setupEmployee.getUsername(), employeeDTO.username());
        assertEquals(setupEmployee.getFullname(), employeeDTO.fullname());
        verify(passwordEncoder).encode(argThat(arg -> !arg.equals("decodedSECRET")));
        verify(employeeRepositoryMock).save(argThat(arg -> arg.getPassword().equals("decodedSECRET")));
        verify(authTokenRepositoryMock, times(1)).deleteByEmployeeId(employeeDTO.id());
    }

    @Test
    @DisplayName("Should throw notFound entity exception when request entity not found")
    void resetEmployeePassword_notFOundEntity_throwNotFound(){
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> employeeServiceMock.resetEmployeePassword(setupEmployee.getId(), ""));
    }

    @Test
    @DisplayName("Should return employee DTO when updated successfully")
    void updateEmployee_validRequest_returnEmployeeResponseDTO(){
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee)); 
        
        EmployeeUpdateRequest employeeUpdateRequest = EmployeeUpdateRequest.builder()
        .fullname("NEW NAME")
        .role("FINANCE")
        .isActive(false)
        .build();

        EmployeeResponse employeeDTO = employeeServiceMock.updateEmployee(setupEmployee.getId(), employeeUpdateRequest);

        assertEquals(setupEmployee.getUsername(), employeeDTO.username());
        assertEquals("NEW NAME", employeeDTO.fullname()); 
        assertEquals(EmployeeRole.FINANCE, employeeDTO.role());
        verify(authTokenRepositoryMock, times(1)).deleteByEmployeeId(employeeDTO.id());
        verify(employeeMapperImpl, times(1)).updateEmployeeFromDto(employeeUpdateRequest, setupEmployee);
    }

    @Test
    @DisplayName("Should thrown NotFoundEntity when request employee not found")
    void updateEmployee_notFoundEntity_throwNotFound(){
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.empty()); 
        
        EmployeeUpdateRequest employeeUpdateRequest = EmployeeUpdateRequest.builder()
        .fullname("NEW NAME")
        .build();

        assertThrows(NotFoundEntityException.class, () -> employeeServiceMock.updateEmployee(setupEmployee.getId(), employeeUpdateRequest)); 
    }

    @Test
    @DisplayName("Should thrown duplicateEntity when request username already exists")
    void updateEmployee_duplicateEntity_throwDuplicate(){
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee)); 
        when(employeeRepositoryMock.existsByUsername("NEW NAME")).thenReturn(true);
        EmployeeUpdateRequest employeeUpdateRequest = EmployeeUpdateRequest.builder()
        .username("NEW NAME")
        .build();

        assertThrows(DuplicateEntityException.class, () -> employeeServiceMock.updateEmployee(setupEmployee.getId(), employeeUpdateRequest)); 
    }

    @Test
    void addCustomerPicture_validRequest() throws IOException{
        CloudinaryResponse cloudinaryResponse = CloudinaryResponse.builder()
        .secureUrl("testUrl.test")
        .publicId("id123")
        .build();
        
        MultipartFile file = new MockMultipartFile("test", "faa".getBytes());
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee));
        when(storageService.uploadImageSync(any(), any(), any())).thenReturn(cloudinaryResponse);

        assertTrue(employeeServiceMock.setEmployeeProfile(setupEmployee.getId(), file));

        verify(storageService, times(1)).uploadImageSync(argThat(arg -> arg.length > 0), any(), eq("employee"));
        verify(employeePictureRepository, times(1)).save(argThat(arg -> arg.getPictureUrl().equals(cloudinaryResponse.getSecureUrl())));
  
        assertEquals(cloudinaryResponse.getSecureUrl(), setupEmployee.getPictureUrl());
    }

    @Test
    void addCustomerPicture_failToUpload() throws IOException{ 
        MultipartFile file = new MockMultipartFile("test", "faa".getBytes());
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee));
        when(storageService.uploadImageSync(any(), any(), any())).thenThrow(IOException.class);
        assertThrows(StorageActionException.class, () -> employeeServiceMock.setEmployeeProfile(setupEmployee.getId(), file)); 
    }

    @Test
    void addCustomerPicture_failToSaveDB() throws IOException{
        CloudinaryResponse cloudinaryResponse = CloudinaryResponse.builder()
        .secureUrl("testUrl.test")
        .publicId("id123")
        .build();
        
        MultipartFile file = new MockMultipartFile("test", "faa".getBytes());
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.of(setupEmployee));
        when(storageService.uploadImageSync(any(), any(), any())).thenReturn(cloudinaryResponse);
        when(employeePictureRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DatabaseInternalException.class, () -> employeeServiceMock.setEmployeeProfile(setupEmployee.getId(), file)); 
    }

    @Test
    void addCustomerPicture_notFound() throws IOException{ 
        MultipartFile file = new MockMultipartFile("test", "faa".getBytes());
        when(employeeRepositoryMock.findById(setupEmployee.getId())).thenReturn(Optional.empty());  
        assertThrows(NotFoundEntityException.class, () -> employeeServiceMock.setEmployeeProfile(setupEmployee.getId(), file));
    }

}
