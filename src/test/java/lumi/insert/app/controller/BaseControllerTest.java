package lumi.insert.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import lumi.insert.app.mapper.AllSupplyMapper;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.mapper.CategoryMapper;
import lumi.insert.app.mapper.ProductMapperImpl;
import lumi.insert.app.service.AuthTokenService;
import lumi.insert.app.service.CategoryService;
import lumi.insert.app.service.CustomerService;
import lumi.insert.app.service.EmployeeService;
import lumi.insert.app.service.MemoService;
import lumi.insert.app.service.PdfService;
import lumi.insert.app.service.ProductService;
import lumi.insert.app.service.SupplierService;
import lumi.insert.app.service.SupplyPaymentService;
import lumi.insert.app.service.SupplyService;
import lumi.insert.app.service.TransactionItemService;
import lumi.insert.app.service.TransactionPaymentService;
import lumi.insert.app.service.TransactionService;
import lumi.insert.app.service.XlsxService;
import lumi.insert.app.service.implement.StockCardServiceImpl;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@WithMockUser(username = "admin")
@ActiveProfiles("test")
public abstract class BaseControllerTest {
    
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthTokenService authTokenService;

    @MockitoBean
    protected CategoryService categoryService;

    @MockitoBean
    protected CustomerService customerService;

    @MockitoBean
    protected EmployeeService employeeService;

    @MockitoBean
    protected MemoService memoService;

    @MockitoBean
    protected ProductService productService;

    @MockitoBean
    protected PdfService pdfService;

    @MockitoBean
    protected TransactionItemService transactionItemService;

    @MockitoBean
    protected StockCardServiceImpl stockCardService;

    @MockitoBean
    protected SupplierService supplierService;

    @MockitoBean
    protected SupplyService supplyService;

    @MockitoBean
    protected XlsxService xlsxService;
    
    @MockitoBean
    protected SupplyPaymentService supplyPaymentService;

    @MockitoBean
    protected TransactionService transactionService; 

    @MockitoBean
    protected TransactionPaymentService transactionPaymentService;

    @Autowired
    protected AllTransactionMapper allTransactionMapper;

    @Autowired
    protected AllSupplyMapper allSupplyMapper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CategoryMapper categoryMapper;
    
    @Autowired
    protected ProductMapperImpl productMapper;


    @BeforeEach
    void setup(WebApplicationContext context) {
    mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity()) 
            .build();
    }

}
