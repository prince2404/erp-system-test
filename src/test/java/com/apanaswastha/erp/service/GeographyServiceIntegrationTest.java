package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.geography.BlockResponse;
import com.apanaswastha.erp.dto.response.geography.CenterResponse;
import com.apanaswastha.erp.dto.request.geography.CreateBlockRequest;
import com.apanaswastha.erp.dto.request.geography.CreateCenterRequest;
import com.apanaswastha.erp.dto.request.geography.CreateDistrictRequest;
import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.geography.DistrictResponse;
import com.apanaswastha.erp.dto.response.geography.StateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GeographyServiceIntegrationTest {

    @Autowired
    private StateService stateService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CenterService centerService;

    @Test
    void shouldCreateAndFetchFullGeographyHierarchy() {
        CreateStateRequest stateRequest = new CreateStateRequest();
        stateRequest.setName("Bihar");
        stateRequest.setCode("BR");
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
        centerRequest.setName("ASK Center 1");
        centerRequest.setCenterCode("ASK-PTN-001");
        centerRequest.setBlockId(block.getId());
        centerRequest.setAddress("Main Road, Patna");
        centerRequest.setContactNumber("9999999999");
        CenterResponse center = centerService.create(centerRequest);

        assertNotNull(state.getId());
        assertNotNull(district.getId());
        assertNotNull(block.getId());
        assertNotNull(center.getId());

        assertEquals(state.getId(), districtService.getById(district.getId()).getStateId());
        assertEquals(district.getId(), blockService.getById(block.getId()).getDistrictId());
        assertEquals(block.getId(), centerService.getById(center.getId()).getBlockId());

        assertEquals(1, stateService.getAll().size());
        assertEquals(1, districtService.getAll().size());
        assertEquals(1, blockService.getAll().size());
        assertEquals(1, centerService.getAll().size());
    }
}
