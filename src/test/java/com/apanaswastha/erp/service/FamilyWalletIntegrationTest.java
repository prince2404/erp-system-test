package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.geography.BlockResponse;
import com.apanaswastha.erp.dto.response.geography.CenterResponse;
import com.apanaswastha.erp.dto.request.geography.CreateBlockRequest;
import com.apanaswastha.erp.dto.request.geography.CreateCenterRequest;
import com.apanaswastha.erp.dto.request.geography.CreateDistrictRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyRequest;
import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.geography.DistrictResponse;
import com.apanaswastha.erp.dto.response.family.FamilyResponse;
import com.apanaswastha.erp.dto.response.geography.StateResponse;
import com.apanaswastha.erp.dto.request.wallet.WalletTransactionRequest;
import com.apanaswastha.erp.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FamilyWalletIntegrationTest {

    @Autowired
    private StateService stateService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private WalletService walletService;

    @Test
    void shouldRegisterFamilyAddMemberAndFetchDetails() {
        CenterResponse center = createCenterHierarchy();

        CreateFamilyRequest familyRequest = new CreateFamilyRequest();
        familyRequest.setFamilyHeadName("Ram Prasad");
        familyRequest.setCenterId(center.getId());
        FamilyResponse registered = familyService.registerFamily(familyRequest);

        assertNotNull(registered.getId());
        assertEquals(new BigDecimal("0.00"), registered.getWalletBalance());
        assertTrue(registered.getHealthCardNumber().matches("^ASK-BH-[A-Z0-9]+-\\d{3}-\\d{5}$"));
        assertTrue(registered.getQrCodeReference().startsWith("QR-ASK-BH-"));

        CreateFamilyMemberRequest memberRequest = new CreateFamilyMemberRequest();
        memberRequest.setFirstName("Sita");
        memberRequest.setLastName("Devi");
        memberRequest.setDob(LocalDate.of(1998, 1, 15));
        memberRequest.setGender("FEMALE");
        memberRequest.setBloodGroup("O+");
        familyService.addFamilyMember(registered.getHealthCardNumber(), memberRequest);

        FamilyResponse fetched = familyService.getByHealthCardNumber(registered.getHealthCardNumber());
        assertEquals(1, fetched.getMembers().size());
        assertEquals("Sita", fetched.getMembers().get(0).getFirstName());
    }

    @Test
    void shouldHandleCreditDebitIdempotencyAndInsufficientBalance() {
        CenterResponse center = createCenterHierarchy();

        CreateFamilyRequest familyRequest = new CreateFamilyRequest();
        familyRequest.setFamilyHeadName("Gopal Kumar");
        familyRequest.setCenterId(center.getId());
        FamilyResponse registered = familyService.registerFamily(familyRequest);

        WalletTransactionRequest creditRequest = new WalletTransactionRequest();
        creditRequest.setHealthCardNumber(registered.getHealthCardNumber());
        creditRequest.setAmount(new BigDecimal("100.00"));
        creditRequest.setReferenceId("credit-ref-1");
        creditRequest.setDescription("Wallet top-up");
        walletService.credit(creditRequest);

        WalletTransactionRequest debitRequest = new WalletTransactionRequest();
        debitRequest.setHealthCardNumber(registered.getHealthCardNumber());
        debitRequest.setAmount(new BigDecimal("40.00"));
        debitRequest.setReferenceId("debit-ref-1");
        debitRequest.setDescription("Consultation fee");
        walletService.debit(debitRequest);

        FamilyResponse updated = familyService.getByHealthCardNumber(registered.getHealthCardNumber());
        assertEquals(new BigDecimal("60.00"), updated.getWalletBalance());
        assertEquals(2, walletService.getTransactions(registered.getHealthCardNumber()).size());

        WalletTransactionRequest duplicateReference = new WalletTransactionRequest();
        duplicateReference.setHealthCardNumber(registered.getHealthCardNumber());
        duplicateReference.setAmount(new BigDecimal("10.00"));
        duplicateReference.setReferenceId("debit-ref-1");
        duplicateReference.setDescription("Duplicate attempt");
        IllegalArgumentException duplicateException = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.credit(duplicateReference)
        );
        assertFalse(duplicateException.getMessage().isBlank());

        WalletTransactionRequest insufficientDebit = new WalletTransactionRequest();
        insufficientDebit.setHealthCardNumber(registered.getHealthCardNumber());
        insufficientDebit.setAmount(new BigDecimal("80.00"));
        insufficientDebit.setReferenceId("debit-ref-2");
        insufficientDebit.setDescription("Overdraw attempt");
        assertThrows(InsufficientBalanceException.class, () -> walletService.debit(insufficientDebit));
    }

    private CenterResponse createCenterHierarchy() {
        CreateStateRequest stateRequest = new CreateStateRequest();
        stateRequest.setName("Bihar");
        stateRequest.setCode("BH");
        StateResponse state = stateService.create(stateRequest);

        CreateDistrictRequest districtRequest = new CreateDistrictRequest();
        districtRequest.setName("Patna");
        districtRequest.setStateId(state.getId());
        DistrictResponse district = districtService.create(districtRequest);

        CreateBlockRequest blockRequest = new CreateBlockRequest();
        blockRequest.setName("Patna Sadar");
        blockRequest.setDistrictId(district.getId());
        BlockResponse block = blockService.create(blockRequest);

        CreateCenterRequest centerRequest = new CreateCenterRequest();
        centerRequest.setName("ASK Center Wallet");
        centerRequest.setCenterCode("ASK-PTN-001");
        centerRequest.setBlockId(block.getId());
        centerRequest.setAddress("Main Road, Patna");
        centerRequest.setContactNumber("8888888888");
        return centerService.create(centerRequest);
    }
}
