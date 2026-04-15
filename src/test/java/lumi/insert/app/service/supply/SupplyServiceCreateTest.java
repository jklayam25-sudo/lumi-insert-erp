package lumi.insert.app.service.supply;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.entity.Supply;
import lumi.insert.app.core.entity.SupplyItem;
import lumi.insert.app.core.entity.nondatabase.SupplyStatus;
import lumi.insert.app.dto.request.SupplyCreateRequest;
import lumi.insert.app.dto.request.SupplyItemCreate;
import lumi.insert.app.dto.response.SupplyResponse;
import lumi.insert.app.exception.NotFoundEntityException;  

public class SupplyServiceCreateTest extends BaseSupplyServiceTest {
    
    @Test
    @DisplayName("Should return SupplyResponse DTO when creating supply is successful")
    public void createSupply_validRequest_returnSupplyResponse() {
        setupProduct.setBasePrice(BigDecimal.valueOf(350L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(0L));

        Supplier supplier = Supplier.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .totalUnpaid(BigDecimal.valueOf(10L))
            .build();

        when(supplyRepositoryMock.saveAndFlush(any(Supply.class))).thenAnswer(i -> i.getArgument(0));
        when(supplierRepositoryMock.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepositoryMock.findAllById(List.of(setupProduct.getId()))).thenReturn(List.of(setupProduct));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
            .supplierId(supplier.getId())
            .invoiceId("INV-XXX-XXX")
            .totalFee(BigDecimal.valueOf(0L))
            .totalDiscount(BigDecimal.valueOf(0L))
            .supplyItems(List.of(SupplyItemCreate.builder()
                .productId(setupProduct.getId())
                .price(BigDecimal.valueOf(400L))
                .quantity(BigDecimal.valueOf(27L))
                .build()))
            .build();

        SupplyResponse supply = supplyServiceMock.createSupply(request);
 
        assertNotNull(supply.invoiceId());
        assertTrue(BigDecimal.valueOf(10800L).compareTo(supply.grandTotal()) == 0); // 400 * 27
        assertEquals(1L, supply.totalItems());
        assertTrue(BigDecimal.valueOf(10800L).compareTo(supply.totalUnpaid()) == 0);
        assertEquals(SupplyStatus.UNPAID, supply.status());

        assertTrue(BigDecimal.valueOf(400L).compareTo(setupProduct.getBasePrice()) == 0);
        assertTrue(BigDecimal.valueOf(27L).compareTo(setupProduct.getStockQuantity()) == 0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<SupplyItem>> supplyItemCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(supplyItemRepositoryMock, times(1)).saveAll(supplyItemCaptor.capture());
        
        SupplyItem createdSupplyItem = supplyItemCaptor.getValue().iterator().next();
        assertTrue(BigDecimal.valueOf(400L).compareTo(createdSupplyItem.getPrice()) == 0);
        assertTrue(BigDecimal.valueOf(27L).compareTo(createdSupplyItem.getQuantity()) == 0);
        assertEquals(setupProduct.getId(), createdSupplyItem.getProduct().getId()); 

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<StockCard>> stockCardCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(stockCardRepositoryMock, times(1)).saveAll(stockCardCaptor.capture());
        
        StockCard createdStockCard = stockCardCaptor.getValue().iterator().next();
        assertTrue(BigDecimal.valueOf(27L).compareTo(createdStockCard.getNewStock()) == 0);
        assertTrue(BigDecimal.valueOf(0L).compareTo(createdStockCard.getOldStock()) == 0); 

        assertTrue(BigDecimal.valueOf(10810L).compareTo(supplier.getTotalUnpaid()) == 0); // 10800 + 10
    }

    @Test
    @DisplayName("Should return SupplyResponse DTO when creating supply is successful, CASE 2: Stock still available, Test of AVG PRICE Sync")
    public void createSupply_validRequest2_returnSupplyResponse() {
        setupProduct.setBasePrice(BigDecimal.valueOf(385L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(13L));

        Supplier supplier = Supplier.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .totalUnpaid(BigDecimal.valueOf(10L))
            .build();

        when(supplyRepositoryMock.saveAndFlush(any(Supply.class))).thenAnswer(i -> i.getArgument(0));
        when(supplierRepositoryMock.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepositoryMock.findAllById(List.of(setupProduct.getId()))).thenReturn(List.of(setupProduct));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
            .supplierId(supplier.getId())
            .invoiceId("INV-XXX-XXX")
            .totalFee(BigDecimal.valueOf(0L))
            .totalDiscount(BigDecimal.valueOf(0L))
            .supplyItems(List.of(SupplyItemCreate.builder()
                .productId(setupProduct.getId())
                .price(BigDecimal.valueOf(400L))
                .quantity(BigDecimal.valueOf(27L))
                .build()))
            .build();

        SupplyResponse supply = supplyServiceMock.createSupply(request);
 
        assertNotNull(supply.invoiceId());
        assertTrue(BigDecimal.valueOf(10800L).compareTo(supply.grandTotal()) == 0);
        assertEquals(1L, supply.totalItems());
        assertTrue(BigDecimal.valueOf(10800L).compareTo(supply.totalUnpaid()) == 0);
        assertEquals(SupplyStatus.UNPAID, supply.status());

        assertTrue(new BigDecimal("395.1250").compareTo(setupProduct.getBasePrice()) == 0);
        assertTrue(BigDecimal.valueOf(40L).compareTo(setupProduct.getStockQuantity()) == 0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<SupplyItem>> supplyItemCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(supplyItemRepositoryMock, times(1)).saveAll(supplyItemCaptor.capture());
        
        SupplyItem createdSupplyItem = supplyItemCaptor.getValue().iterator().next();
        assertTrue(BigDecimal.valueOf(400L).compareTo(createdSupplyItem.getPrice()) == 0);
        assertTrue(BigDecimal.valueOf(27L).compareTo(createdSupplyItem.getQuantity()) == 0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<StockCard>> stockCardCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(stockCardRepositoryMock, times(1)).saveAll(stockCardCaptor.capture());
        
        StockCard createdStockCard = stockCardCaptor.getValue().iterator().next();
        assertTrue(BigDecimal.valueOf(40L).compareTo(createdStockCard.getNewStock()) == 0);
        assertTrue(BigDecimal.valueOf(13L).compareTo(createdStockCard.getOldStock()) == 0); 

        assertTrue(BigDecimal.valueOf(10810L).compareTo(supplier.getTotalUnpaid()) == 0);
    }

    @Test
    @DisplayName("Should throw notFound when supplier not found")
    public void createSupply_invalidSupplier_throwNotFound() { 
        when(supplierRepositoryMock.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> 
            supplyServiceMock.createSupply(SupplyCreateRequest.builder()
                .supplierId(setupSupply.getId())
                .build()));
    }

    @Test
    @DisplayName("Should throw notFound when one or more of product request to add not found")
    public void createSupply_requestProductToAddNotFound_throwNotFound() { 
        Supplier supplier = Supplier.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .totalUnpaid(BigDecimal.valueOf(10L))
            .build();
 
        when(supplierRepositoryMock.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepositoryMock.findAllById(anyIterable())).thenReturn(List.of(setupProduct));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
            .supplierId(supplier.getId())
            .invoiceId("INV-XXX-XXX")
            .totalFee(BigDecimal.valueOf(0L))
            .totalDiscount(BigDecimal.valueOf(0L))
            .supplyItems(List.of(
                SupplyItemCreate.builder().productId(setupProduct.getId()).price(BigDecimal.valueOf(400L)).quantity(BigDecimal.valueOf(27L)).build(),
                SupplyItemCreate.builder().productId(777L).price(BigDecimal.valueOf(400L)).quantity(BigDecimal.valueOf(27L)).build()
            ))
            .build();

        assertThrows(NotFoundEntityException.class, () -> supplyServiceMock.createSupply(request));
    }
}