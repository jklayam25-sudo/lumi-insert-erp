package lumi.insert.app.service.memo;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import lumi.insert.app.core.repository.EmployeeRepository;
import lumi.insert.app.core.repository.MemoRepository;
import lumi.insert.app.core.repository.MemoViewRepository;
import lumi.insert.app.mapper.MemoMapper;
import lumi.insert.app.service.implement.MemoServiceImpl;
import lumi.insert.app.mapper.MemoMapperImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseMemoServiceTest {
    
    @InjectMocks
    MemoServiceImpl memoService;

    @Mock
    MemoRepository memoRepository;

    @Mock
    MemoViewRepository memoViewRepository;

    @Mock
    EmployeeRepository employeeRepository;

    @Spy
    MemoMapper memoMapper = new MemoMapperImpl();

}
