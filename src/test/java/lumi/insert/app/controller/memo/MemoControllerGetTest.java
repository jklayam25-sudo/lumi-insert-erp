package lumi.insert.app.controller.memo;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.dto.response.MemoResponse;
import lumi.insert.app.exception.NotFoundEntityException;

public class MemoControllerGetTest extends BaseMemoControllerTest{
    
    @Test
    void getMemoAPI_foundEntity_returnOkAndDTO() throws Exception{
        when(memoService.getMemo(1L)).thenReturn(memoResponse);

        mockMvc.perform(
            get("/api/memos/1")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.id").value(memoResponse.id()));
    }

    @Test
    void getMemoAPI_notFoundEntity_returnOkAndDTO() throws Exception{
        when(memoService.getMemo(2L)).thenThrow(new NotFoundEntityException("Memo with ID " + 2L + " was not found"));

        mockMvc.perform(
            get("/api/memos/2")
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Memo with ID " + 2L + " was not found"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getMemosAPI_foundEntity_returnOkAndDTO() throws Exception{
        Slice<MemoResponse> slices = new SliceImpl<>(List.of(memoResponse));
        when(memoService.getMemos(any(EmployeeLogin.class), any(LocalDateTime.class))).thenReturn(slices);

        mockMvc.perform(
            get("/api/memos") 
            .with(authentication(auth))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content.length()").value(1));

        verify(memoService, times(1)).getMemos(argThat(arg -> arg.getUsername().equals("lumi")), argThat(arg -> arg.getDayOfMonth() == (LocalDateTime.now().minusMonths(1).getDayOfMonth())));
    }

    @Test
    void getMemosAPI_datedFoundEntity_returnOkAndDTO() throws Exception{
        Slice<MemoResponse> slices = new SliceImpl<>(List.of(memoResponse));
        when(memoService.getMemos(any(EmployeeLogin.class), any(LocalDateTime.class))).thenReturn(slices);

        mockMvc.perform(
            get("/api/memos?updatedAt=" + LocalDateTime.now().minusDays(7)) 
            .with(authentication(auth))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content.length()").value(1));

        verify(memoService, times(1)).getMemos(argThat(arg -> arg.getUsername().equals("lumi")), argThat(arg -> arg.getDayOfMonth() == (LocalDateTime.now().minusDays(7).getDayOfMonth())));
    }

    @Test
    void getMemosAPI_notFoundEntity_returnOkAndDTO() throws Exception{
        Slice<MemoResponse> slices = new SliceImpl<>(List.of());
        when(memoService.getMemos(any(EmployeeLogin.class), any(LocalDateTime.class))).thenReturn(slices);

        mockMvc.perform(
            get("/api/memos") 
            .with(authentication(auth))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content.length()").value(0)); 
    }
}
