package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.CreateFamilyRequest;
import com.apanaswastha.erp.dto.FamilyMemberResponse;
import com.apanaswastha.erp.dto.FamilyResponse;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.entity.Family;
import com.apanaswastha.erp.entity.FamilyMember;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.FamilyMemberRepository;
import com.apanaswastha.erp.repository.FamilyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FamilyService {

    private static final String DEFAULT_STATE_CODE = "BH";
    private static final String DEFAULT_DISTRICT_CODE = "GEN";

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final CenterRepository centerRepository;

    public FamilyService(
            FamilyRepository familyRepository,
            FamilyMemberRepository familyMemberRepository,
            CenterRepository centerRepository
    ) {
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.centerRepository = centerRepository;
    }

    @Transactional
    public FamilyResponse registerFamily(CreateFamilyRequest request) {
        Center center = centerRepository.findById(request.getCenterId())
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + request.getCenterId()));

        Family family = new Family();
        family.setFamilyHeadName(request.getFamilyHeadName());
        family.setCenter(center);
        family.setWalletBalance(new BigDecimal("0.00"));
        family.setHealthCardNumber("PENDING-" + UUID.randomUUID().toString().replace("-", ""));
        family.setQrCodeReference("PENDING-" + UUID.randomUUID());

        Family savedFamily = familyRepository.save(family);

        String healthCardNumber = generateHealthCardNumber(savedFamily);
        savedFamily.setHealthCardNumber(healthCardNumber);
        savedFamily.setQrCodeReference("QR-" + healthCardNumber);

        return toFamilyResponse(familyRepository.save(savedFamily));
    }

    @Transactional
    public FamilyMemberResponse addFamilyMember(String healthCardNumber, CreateFamilyMemberRequest request) {
        Family family = familyRepository.findByHealthCardNumber(healthCardNumber)
                .orElseThrow(() -> new NotFoundException("Family not found with health card number: " + healthCardNumber));

        FamilyMember member = new FamilyMember();
        member.setFamily(family);
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setDob(request.getDob());
        member.setGender(request.getGender());
        member.setBloodGroup(request.getBloodGroup());

        return toMemberResponse(familyMemberRepository.save(member));
    }

    public FamilyResponse getByHealthCardNumber(String healthCardNumber) {
        Family family = familyRepository.findByHealthCardNumber(healthCardNumber)
                .orElseThrow(() -> new NotFoundException("Family not found with health card number: " + healthCardNumber));
        return toFamilyResponse(family);
    }

    private FamilyResponse toFamilyResponse(Family family) {
        List<FamilyMemberResponse> members = familyMemberRepository.findAllByFamilyId(family.getId()).stream()
                .map(this::toMemberResponse)
                .toList();

        return new FamilyResponse(
                family.getId(),
                family.getFamilyHeadName(),
                family.getHealthCardNumber(),
                family.getQrCodeReference(),
                family.getWalletBalance(),
                family.getCenter().getId(),
                family.getCreatedAt(),
                family.getUpdatedAt(),
                members
        );
    }

    private FamilyMemberResponse toMemberResponse(FamilyMember member) {
        return new FamilyMemberResponse(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getDob(),
                member.getGender(),
                member.getBloodGroup(),
                member.getFamily().getId(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    private String generateHealthCardNumber(Family family) {
        String stateCode = DEFAULT_STATE_CODE;
        if (family.getCenter() != null
                && family.getCenter().getBlock() != null
                && family.getCenter().getBlock().getDistrict() != null
                && family.getCenter().getBlock().getDistrict().getState() != null
                && family.getCenter().getBlock().getDistrict().getState().getCode() != null
                && !family.getCenter().getBlock().getDistrict().getState().getCode().isBlank()) {
            stateCode = family.getCenter().getBlock().getDistrict().getState().getCode().toUpperCase();
        }

        String districtCode = extractDistrictCode(family.getCenter().getCenterCode());

        String centerSegment = String.format("%03d", family.getCenter().getId());
        String familySegment = String.format("%05d", family.getId());
        return "ASK-" + stateCode + "-" + districtCode + "-" + centerSegment + "-" + familySegment;
    }

    private String extractDistrictCode(String centerCode) {
        if (centerCode == null || centerCode.isBlank()) {
            return DEFAULT_DISTRICT_CODE;
        }

        String[] parts = centerCode.split("-");
        if (parts.length < 2) {
            return DEFAULT_DISTRICT_CODE;
        }

        String code = parts[1].replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return code.isBlank() ? DEFAULT_DISTRICT_CODE : code;
    }
}
