package lumi.insert.app.service.implement;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service; 
import org.springframework.web.multipart.MultipartFile;

import com.github.f4b6a3.uuid.UuidCreator;
 
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.entity.Supply;
import lumi.insert.app.core.entity.SupplyPayment;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EntityList;
import lumi.insert.app.core.entity.nondatabase.SupplyStatus;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.core.repository.SupplyPaymentRepository;
import lumi.insert.app.core.repository.SupplyRepository;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.SupplyPaymentCreateRequest;
import lumi.insert.app.dto.request.SupplyPaymentGetByFilter;
import lumi.insert.app.dto.response.SupplyPaymentResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.StorageActionException;
import lumi.insert.app.exception.TransactionValidationException;
import lumi.insert.app.mapper.AllSupplyMapper; 
import lumi.insert.app.service.SupplyPaymentService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

/**
 * Implementation of {@link SupplyPaymentService} managing supply settlements and proof of payment.
 * <p>
 * This service handles the final stages of the supply lifecycle, ensuring that cash inflows 
 * and outflows (refunds) are accurate.
 * </p>
 * * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li><b>Payment Processing:</b> Records company payments to supplier, reduces unpaid balances, and 
 * adjust when there's refund.</li> 
 * <li><b>Asynchronous Evidence Storage:</b> Handles payment proof (receipts/images) by 
 * transferring {@link MultipartFile} to temporary storage and publishing 
 * {@link UploadStorageMessage} events for background processing.</li>
 * <li><b>Financial Integrity:</b> Prevents overpayment or over-refunding through strict 
 * {@link BigDecimal} validation.</li>
 * </ul>
 *
 * @author KelvinKhodes
 * @since 1.0.0
 */
@Service
@Transactional
@Slf4j
public class SupplyPaymentServiceImpl implements SupplyPaymentService{

    @Autowired
    SupplyPaymentRepository supplyPaymentRepository;

    @Autowired
    SupplyRepository supplyRepository;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Autowired
    AllSupplyMapper allSupplyMapper;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    /**
     * Processes a new payment for a supply.
     * Validates that the supply is UNPAID and the amount doesn't exceed the debt.
     * Updates unpaid/paid balances for both Supply and Supplier.
     * * @param supplyId The ID of the supply to pay for.
     * @param request Contains payment details and optional file attachments.
     * @return DTO representation of the created payment.
     * @throws TransactionValidationException if payment exceeds the remaining debt.
     */
    @Override
    @ActivityLogger(
        entityName = "supply_payments",
        action = ActivityAction.SUPPLY_PAYMENT_SETTLED,
        actionMessage = "Supply payment settled to supplier"
    )
    public SupplyPaymentResponse createSupplyPayment(UUID supplyId, SupplyPaymentCreateRequest request) {
        log.info("Creating supply payment for supplyId={}, amount={}", supplyId, request.getTotalPayment());
        Supply supply = supplyRepository.findById(supplyId)
            .orElseThrow(() -> {
                log.debug("Supply payment creation failed, supply not found id={}", supplyId);
                return new NotFoundEntityException("Supply with ID " + supplyId + " was not found");
            });

        if(supply.getStatus() != SupplyStatus.UNPAID) {
            log.debug("Supply payment creation failed, invalid supply status id={}, status={}", supplyId, supply.getStatus());
            throw new ForbiddenRequestException("Unable to set payment because supply status is not UNPAID, check carefully");
        }

        SupplyPayment supplyPayment = SupplyPayment.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .supply(supply)
            .paymentFrom(request.getPaymentFrom())
            .paymentTo(request.getPaymentTo())
            .totalPayment(request.getTotalPayment())
            .build();

        supply.setTotalUnpaid(supply.getTotalUnpaid().subtract(request.getTotalPayment()));
        supply.setTotalPaid(supply.getTotalPaid().add(request.getTotalPayment()));

        // Check if request payment exceeds the unpaid left
        if(supply.getTotalUnpaid().compareTo(BigDecimal.ZERO) < 0) {
            log.debug("Supply payment exceeds unpaid amount supplyId={}, requestedPayment={}", supplyId, request.getTotalPayment());
            throw new TransactionValidationException("Payment exceeds the remaining transaction debts with ID " + supplyId + ", enter an exact amount to proceed");
        }

        Supplier supplier = supply.getSupplier();
        supplier.setTotalUnpaid(supplier.getTotalUnpaid().subtract(request.getTotalPayment()));
        supplier.setTotalPaid(supplier.getTotalPaid().add(request.getTotalPayment()));
        // Upcoming: integrate with email notification
        if(supply.getTotalUnpaid().compareTo(BigDecimal.ZERO) == 0) supply.setStatus(SupplyStatus.COMPLETE);
        SupplyPayment savedSupplyPayment = supplyPaymentRepository.save(supplyPayment);
        SupplyPaymentResponse supplyPaymentResponse = allSupplyMapper.createSupplyPaymentResponseDto(savedSupplyPayment);

        // Upload payment proof/others to storage via message producer
        MultipartFile[] files = request.getFiles();
        List<Path> paths = new ArrayList<>(); 
        try {
            for (MultipartFile file : files) {
                // Save to temporary
                Path tempFile = Files.createTempFile("paymentOf" + supply.getId() + "-", "_upload");
                file.transferTo(tempFile);
                paths.add(tempFile);
                
            }    
        } catch (IOException e) {
            log.error("Store file failed for supplyId={}, messages={}", supplyId, e.getMessage());
            throw new StorageActionException("Server couldn't complete the request due to internal problem, try again or contact developer");
        }
        
         paths.forEach(path -> {
            // Publish event to EventListener < listen and pass msg producer service
            eventPublisher.publishEvent(
                new UploadStorageMessage(
                    EntityList.SUPPLY_PAYMENT,
                    supplyPayment.getId(), 
                    path.toAbsolutePath().toString(),
                    ((EmployeeLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                )
            );
        });       
  
        log.info("Supply payment created paymentId={}, supplyId={}", savedSupplyPayment.getId(), supplyId);
        return supplyPaymentResponse;
    }

    /**
     * Retrieves a paginated slice of payments associated with a specific supply.
     */
    @Override 
    public Slice<SupplyPaymentResponse> getSupplyPaymentsBySupplyId(UUID supplyId, PaginationRequest request) {
        log.info("Retrieving supply payments for supplyId={}, page={}, size={}", supplyId, request.getPage(), request.getSize());
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Slice<SupplyPayment> payments = supplyPaymentRepository.findAllBySupplyId(supplyId, pageable); 
        return payments.map(allSupplyMapper::createSupplyPaymentResponseDto);
    }

    @Override
    public SupplyPaymentResponse getSupplyPayment(UUID id) {
        log.info("Retrieving supply payment id={}", id);
        SupplyPayment supplyPayment = supplyPaymentRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Supply payment not found id={}", id);
                return new NotFoundEntityException("Supply payment with ID " + id + " was not found");
            });

        return allSupplyMapper.createSupplyPaymentResponseDto(supplyPayment);
    }

    @Override
    public Slice<SupplyPaymentResponse> getSupplyPaymentsByRequests(SupplyPaymentGetByFilter request) {
        log.info("Searching supply payments with filters page={}, size={}", request.getPage(), request.getSize());
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Specification<SupplyPayment> supplyPaymentSpecification = jpaSpecGenerator.supplyPaymentSpecification(request);

        Slice<SupplyPayment> payments = supplyPaymentRepository.findAll(supplyPaymentSpecification, pageable);
        return payments.map(allSupplyMapper::createSupplyPaymentResponseDto);
    }

    /**
     * Processes a refund received from a supplier.
     * Similar to payment creation but targets the refund balances and 
     * sets the 'isForRefund' flag to true.
     * * @param supplyId The ID of the supply being refunded.
     * @param request Contains refund details and proof files.
     * @return DTO representation of the created refund record.
     */
    @Override
    @ActivityLogger(
        entityName = "supply_payments",
        action = ActivityAction.SUPPLY_REFUND_RECEIVED,
        actionMessage = "Supply payment refund received from supplier"
    )
    public SupplyPaymentResponse refundSupplyPayment(UUID supplyId, SupplyPaymentCreateRequest request) {
        log.info("Creating supply refund payment for supplyId={}, amount={}", supplyId, request.getTotalPayment());
        Supply supply = supplyRepository.findById(supplyId)
            .orElseThrow(() -> {
                log.debug("Refund failed, supply not found id={}", supplyId);
                return new NotFoundEntityException("Supply with ID " + supplyId + " was not found");
            });

        if(supply.getStatus() == SupplyStatus.UNPAID) {
            log.debug("Refund payment attempted on invalid supply status supplyId={}, status={}", supplyId, supply.getStatus());
            throw new ForbiddenRequestException("Unable to set payment because supply status is UNPAID / NOT DONE YET, check carefully");
        }

        SupplyPayment supplyPayment = SupplyPayment.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .supply(supply)
            .paymentFrom(request.getPaymentFrom())
            .paymentTo(request.getPaymentTo())
            .totalPayment(request.getTotalPayment())
            .isForRefund(true)
            .build();

        supply.setTotalUnrefunded(supply.getTotalUnrefunded().subtract(request.getTotalPayment()));
        supply.setTotalRefunded(supply.getTotalRefunded().add(request.getTotalPayment()));

        // Check if request payment exceeds the unpaid left
        if(supply.getTotalUnrefunded().compareTo(BigDecimal.ZERO) < 0) {
            log.debug("Refund exceeds unrefunded amount supplyId={}, requestAmount={}", supplyId, request.getTotalPayment());
            throw new TransactionValidationException("Payment exceeds the remaining transaction debts with ID " + supplyId + ", enter an exact amount to proceed");
        }

        Supplier supplier = supply.getSupplier();
        supplier.setTotalUnrefunded(supplier.getTotalUnrefunded().subtract(request.getTotalPayment()));
        supplier.setTotalRefunded(supplier.getTotalRefunded().add(request.getTotalPayment()));
        // Upcoming: integrate with email notification 
        SupplyPayment savedSupplyPayment = supplyPaymentRepository.save(supplyPayment);
        SupplyPaymentResponse supplyPaymentResponse = allSupplyMapper.createSupplyPaymentResponseDto(savedSupplyPayment);

        // Upload payment proof/others to storage via message producer
        MultipartFile[] files = request.getFiles();
        List<Path> paths = new ArrayList<>(); 
        try {
            for (MultipartFile file : files) {
                // Save to temporary
                Path tempFile = Files.createTempFile("refPaymentOf" + supply.getId() + "-", "_upload");
                file.transferTo(tempFile);
                paths.add(tempFile);
                
            }    
        } catch (IOException e) {
            log.error("Store file failed for supplyId={}, messages={}", supplyId, e.getMessage());
            throw new StorageActionException("Server couldn't complete the request due to internal problem, try again or contact developer");
        }
        
         paths.forEach(path -> {
            // Publish event to EventListener < listen and pass msg producer service
            eventPublisher.publishEvent(
                new UploadStorageMessage(
                    EntityList.SUPPLY_PAYMENT,
                    savedSupplyPayment.getId(), 
                    path.toAbsolutePath().toString(),
                    ((EmployeeLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                )
            );
        });       

        log.info("Supply refund payment created paymentId={}, supplyId={}", savedSupplyPayment.getId(), supplyId);
        return supplyPaymentResponse;
    }
    
}
