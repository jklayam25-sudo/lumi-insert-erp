package lumi.insert.app.service;

import java.time.LocalDateTime; 

import org.springframework.data.domain.Slice;

import lumi.insert.app.core.entity.MemoView;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.dto.request.MemoCreateRequest;
import lumi.insert.app.dto.request.MemoUpdateRequest;
import lumi.insert.app.dto.response.MemoResponse; 

public interface MemoService {
    MemoResponse createMemo(MemoCreateRequest request);

    MemoResponse updateMemo(Long id, MemoUpdateRequest request);

    MemoResponse archiveMemo(Long id);

    MemoResponse getMemo(Long id);

    Slice<MemoResponse> getMemos(EmployeeLogin login, LocalDateTime time);

    MemoView createMemoView(EmployeeLogin login, Long id);
}
