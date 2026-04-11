package lumi.insert.app.service.implement;

import java.time.LocalDateTime; 

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.Slice; 
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.Memo;
import lumi.insert.app.core.entity.MemoView;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.repository.EmployeeRepository;
import lumi.insert.app.core.repository.MemoRepository;
import lumi.insert.app.core.repository.MemoViewRepository;
import lumi.insert.app.dto.request.MemoCreateRequest;
import lumi.insert.app.dto.request.MemoUpdateRequest;
import lumi.insert.app.dto.response.MemoResponse;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.mapper.MemoMapper;
import lumi.insert.app.service.MemoService;

@Service
@Slf4j
public class MemoServiceImpl implements MemoService{

    @Autowired
    MemoRepository memoRepository;

    @Autowired
    MemoViewRepository memoViewRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    MemoMapper mapper;

    @Override
    @ActivityLogger(
        entityName = "memos",
        action = ActivityAction.MEMO_CREATED,
        actionMessage = "New memo created"
    )
    public MemoResponse createMemo(MemoCreateRequest request) {
        log.info("Creating memo title={}", request.getTitle());
        Memo memo = Memo.builder()
        .title(request.getTitle())
        .body(request.getBody())
        .build();

        if(request.getRole() != null) memo.setRole(EmployeeRole.valueOf(request.getRole()));
        Memo savedMemo = memoRepository.save(memo);
        log.info("Memo created id={}", savedMemo.getId());

        if(!(request.getImages() == null)){
            // Call method to process the image
        }

        return mapper.createDtoResponseFromMemo(savedMemo);
    }

    @Override
    @ActivityLogger(
        entityName = "memos",
        action = ActivityAction.MEMO_UPDATED,
        actionMessage = "Memo updated"
    )
    public MemoResponse updateMemo(Long id, MemoUpdateRequest request) {
        log.info("Updating memo id={}", id);
        Memo memo = memoRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Memo update failed, not found id={}", id);
                return new NotFoundEntityException("Memo with ID " + id + " was not found");
            });
        
        mapper.updateEntityFromDto(request, memo);

        memoViewRepository.deleteMemoView(id);
        log.info("Memo updated id={}", id);

        return mapper.createDtoResponseFromMemo(memo);
    }

    @Override
    @ActivityLogger(
        entityName = "memos",
        action = ActivityAction.MEMO_UPDATED,
        actionMessage = "Memo is archieved"
    )
    public MemoResponse archiveMemo(Long id) {
        log.info("Archiving memo id={}", id);
        Memo memo = memoRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Memo archive failed, not found id={}", id);
                return new NotFoundEntityException("Memo with ID " + id + " is not found");
            });

        memo.setIsActive(false);
        log.info("Memo archived id={}", id);
        return mapper.createDtoResponseFromMemo(memo);
    }

    @Override
    public MemoResponse getMemo(Long id) {
        log.info("Retrieving memo id={}", id);
        Memo memo = memoRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Memo not found id={}", id);
                return new NotFoundEntityException("Memo with ID " + id + " is not found");
            });
 
        return mapper.createDtoResponseFromMemo(memo);
    }

    @Override
    public Slice<MemoResponse> getMemos(EmployeeLogin login, LocalDateTime time) { 
        return memoRepository.findActiveMemosByRoleOrPublic(login.getId(), login.getRole(), time);
    }

    @Override
    @ActivityLogger(
        entityName = "memo_views",
        action = ActivityAction.MEMO_READ,
        actionMessage = "Memo read"
    )
    public MemoView createMemoView(EmployeeLogin login, Long id) {
        log.info("Creating memo view for memoId={} employeeId={}", id, login.getId());
        try {
            Memo memo = memoRepository.getReferenceById(id);
            Employee employee = employeeRepository.getReferenceById(login.getId());
            MemoView memoView = new MemoView(memo, employee);
            MemoView result = memoViewRepository.save(memoView); 
            log.info("Memo view created memoId={} employeeId={}", id, login.getId());
            return result;
        } catch (Exception e) {
            log.error("Failed to create memo view memoId={} employeeId={} message={}", id, login.getId(), e.getLocalizedMessage());
            log.debug("Memo view exception", e);
            return new MemoView();
        }
    }
    
}
