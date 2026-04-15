package lumi.insert.app.service.memo;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
 
import com.github.f4b6a3.uuid.UuidCreator;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;
import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.Memo;
import lumi.insert.app.core.entity.MemoView;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.dto.request.MemoCreateRequest;
import lumi.insert.app.dto.response.MemoResponse;

public class MemoServiceCreateTest extends BaseMemoServiceTest{
    
    @Test
    void createMemo_validRequest_returnDTO(){
        MemoCreateRequest request = MemoCreateRequest.builder()
        .title("Test Title")
        .body("Body Title")
        .role("FINANCE")
        .build();

        when(memoRepository.save(any(Memo.class))).thenAnswer(arg -> arg.getArgument(0));

        MemoResponse memo = memoService.createMemo(request);
        assertEquals(request.getBody(), memo.body());
        assertNull(memo.isRead());

        verify(memoRepository, times(1)).save(argThat(arg -> arg.getTitle().equals(request.getTitle())  && arg.getRole().equals(EmployeeRole.FINANCE)));
    }

    @Test
    void createMemoView_validRequest_returnTrue(){
        UUID employeeId = UuidCreator.getTimeOrderedEpochFast();

        Employee employee = new Employee(); 
        employee.setId(employeeId);
  
        Memo memo = new Memo();
        memo.setId(1L);

        EmployeeLogin login = EmployeeLogin.builder()
        .id(employeeId)
        .role(EmployeeRole.CASHIER)
        .username("Employee A")
        .build();

        when(memoRepository.getReferenceById(any())).thenReturn(memo);
        when(employeeRepository.getReferenceById(any())).thenReturn(employee);
        when(memoViewRepository.save(any(MemoView.class))).thenAnswer(arg -> arg.getArgument(0));
 
        assertTrue(memoService.createMemoView(login, 1L) != null);
        verify(memoViewRepository, times(1)).save(argThat(arg-> arg.getId().equals("1" + employeeId)));
    }

    @Test
    void createMemoView_invalidRequest_returnFalse(){
        UUID employeeId = UuidCreator.getTimeOrderedEpochFast();

        Employee employee = new Employee(); 
        employee.setId(employeeId);
  
        Memo memo = new Memo();
        memo.setId(1L);

        EmployeeLogin login = EmployeeLogin.builder()
        .id(employeeId)
        .role(EmployeeRole.CASHIER)
        .username("Employee A")
        .build();

        when(memoRepository.getReferenceById(any())).thenReturn(memo);
        when(employeeRepository.getReferenceById(any())).thenReturn(employee);
        when(memoViewRepository.save(any(MemoView.class))).thenThrow(new ConstraintViolationException("", null));
 
        assertTrue(memoService.createMemoView(login, 1L).getId() == null);
        
    }
}
