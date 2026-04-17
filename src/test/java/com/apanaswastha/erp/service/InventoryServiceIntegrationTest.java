package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.BlockResponse;
import com.apanaswastha.erp.dto.CenterResponse;
import com.apanaswastha.erp.dto.CreateBlockRequest;
import com.apanaswastha.erp.dto.CreateCenterRequest;
import com.apanaswastha.erp.dto.CreateDistrictRequest;
import com.apanaswastha.erp.dto.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.CreateMedicineRequest;
import com.apanaswastha.erp.dto.CreateStateRequest;
import com.apanaswastha.erp.dto.DistrictResponse;
import com.apanaswastha.erp.dto.InventoryBatchResponse;
import com.apanaswastha.erp.entity.InventoryBatch;
import com.apanaswastha.erp.entity.Vendor;
import com.apanaswastha.erp.exception.InsufficientStockException;
import com.apanaswastha.erp.repository.InventoryBatchRepository;
import com.apanaswastha.erp.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private StateService stateService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Test
    void shouldDeductFromEarliestExpiryBatchesUsingFifo() {
        CenterResponse center = createCenterHierarchy("INV-FIFO");
        Long medicineId = createMedicine("Paracetamol");
        Long vendorId = createVendor("Vendor A");

        inventoryService.addBatch(createBatchRequest(center.getId(), medicineId, vendorId, "BATCH-OLD", LocalDate.now().plusDays(15), 5));
        inventoryService.addBatch(createBatchRequest(center.getId(), medicineId, vendorId, "BATCH-NEW", LocalDate.now().plusDays(45), 10));

        inventoryService.dispenseMedicine(center.getId(), "Paracetamol", 7);

        List<InventoryBatch> batches = inventoryBatchRepository.findByCenterIdAndQuantityAvailableGreaterThan(center.getId(), -1)
                .stream()
                .sorted(java.util.Comparator.comparing(InventoryBatch::getBatchNumber))
                .toList();

        assertEquals(0, batches.stream().filter(batch -> batch.getBatchNumber().equals("BATCH-OLD")).findFirst().orElseThrow().getQuantityAvailable());
        assertEquals(8, batches.stream().filter(batch -> batch.getBatchNumber().equals("BATCH-NEW")).findFirst().orElseThrow().getQuantityAvailable());

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.dispenseMedicine(center.getId(), "Paracetamol", 100));
    }

    @Test
    void shouldReturnExpiringBatchesForThirtyAndSixtyDayWindows() {
        CenterResponse center = createCenterHierarchy("INV-EXP");
        Long medicineId = createMedicine("Amoxicillin");
        Long vendorId = createVendor("Vendor B");

        inventoryService.addBatch(createBatchRequest(center.getId(), medicineId, vendorId, "EXP-20", LocalDate.now().plusDays(20), 5));
        inventoryService.addBatch(createBatchRequest(center.getId(), medicineId, vendorId, "EXP-45", LocalDate.now().plusDays(45), 5));
        inventoryService.addBatch(createBatchRequest(center.getId(), medicineId, vendorId, "EXP-90", LocalDate.now().plusDays(90), 5));

        List<InventoryBatchResponse> in30Days = inventoryService.getExpiringBatches(center.getId(), 30);
        List<InventoryBatchResponse> in60Days = inventoryService.getExpiringBatches(center.getId(), 60);

        assertEquals(1, in30Days.size());
        assertEquals("EXP-20", in30Days.get(0).getBatchNumber());

        assertEquals(2, in60Days.size());
        assertEquals("EXP-20", in60Days.get(0).getBatchNumber());
        assertEquals("EXP-45", in60Days.get(1).getBatchNumber());
    }

    private Long createMedicine(String name) {
        CreateMedicineRequest request = new CreateMedicineRequest();
        request.setName(name);
        request.setGenericName(name + " Generic");
        request.setManufacturer("Test Manufacturer");
        return inventoryService.addMedicine(request).getId();
    }

    private Long createVendor(String name) {
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setContactInfo("9999999999");
        vendor.setAddress("Patna");
        return vendorRepository.save(vendor).getId();
    }

    private CreateInventoryBatchRequest createBatchRequest(
            Long centerId,
            Long medicineId,
            Long vendorId,
            String batchNumber,
            LocalDate expiryDate,
            Integer quantity
    ) {
        CreateInventoryBatchRequest request = new CreateInventoryBatchRequest();
        request.setMedicineId(medicineId);
        request.setVendorId(vendorId);
        request.setCenterId(centerId);
        request.setBatchNumber(batchNumber);
        request.setExpiryDate(expiryDate);
        request.setQuantityReceived(quantity);
        request.setUnitPrice(new BigDecimal("2.50"));
        request.setSellingPrice(new BigDecimal("3.00"));
        return request;
    }

    private CenterResponse createCenterHierarchy(String codeSuffix) {
        CreateStateRequest stateRequest = new CreateStateRequest();
        stateRequest.setName("Bihar " + codeSuffix);
        stateRequest.setCode(codeSuffix);
        var state = stateService.create(stateRequest);

        CreateDistrictRequest districtRequest = new CreateDistrictRequest();
        districtRequest.setName("Patna " + codeSuffix);
        districtRequest.setStateId(state.getId());
        DistrictResponse district = districtService.create(districtRequest);

        CreateBlockRequest blockRequest = new CreateBlockRequest();
        blockRequest.setName("Bihta " + codeSuffix);
        blockRequest.setDistrictId(district.getId());
        BlockResponse block = blockService.create(blockRequest);

        CreateCenterRequest centerRequest = new CreateCenterRequest();
        centerRequest.setName("Inventory Center " + codeSuffix);
        centerRequest.setCenterCode("BR-" + codeSuffix);
        centerRequest.setAddress("Main Road");
        centerRequest.setContactNumber("9876543210");
        centerRequest.setBlockId(block.getId());

        return centerService.create(centerRequest);
    }
}
