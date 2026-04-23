package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.*;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.enums.UserStatus;
import com.apanaswastha.erp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PermissionProfileRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserPermissionToggleRepository toggleRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private CenterRepository centerRepository;
    @Autowired
    private AuthService authService;

    private State bihar;
    private State jharkhand;
    private District patna;
    private District ranchi;
    private Block patnaBlock;
    private Block ranchiBlock;
    private Center centerA;
    private Center centerB;

    @BeforeEach
    void setUpGeo() {
        bihar = new State();
        bihar.setName("Bihar");
        bihar.setCode("BR");
        stateRepository.save(bihar);

        jharkhand = new State();
        jharkhand.setName("Jharkhand");
        jharkhand.setCode("JH");
        stateRepository.save(jharkhand);

        patna = new District();
        patna.setName("Patna");
        patna.setState(bihar);
        districtRepository.save(patna);

        ranchi = new District();
        ranchi.setName("Ranchi");
        ranchi.setState(jharkhand);
        districtRepository.save(ranchi);

        patnaBlock = new Block();
        patnaBlock.setName("Patna Sadar");
        patnaBlock.setDistrict(patna);
        blockRepository.save(patnaBlock);

        ranchiBlock = new Block();
        ranchiBlock.setName("Ranchi Block");
        ranchiBlock.setDistrict(ranchi);
        blockRepository.save(ranchiBlock);

        centerA = new Center();
        centerA.setName("Center A");
        centerA.setCenterCode("ASK-A");
        centerA.setAddress("A");
        centerA.setContactNumber("9999999999");
        centerA.setBlock(patnaBlock);
        centerRepository.save(centerA);

        centerB = new Center();
        centerB.setName("Center B");
        centerB.setCenterCode("ASK-B");
        centerB.setAddress("B");
        centerB.setContactNumber("8888888888");
        centerB.setBlock(ranchiBlock);
        centerRepository.save(centerB);
    }

    @Test
    void scopeAndHierarchyRulesAreEnforced() throws Exception {
        User stateMgr = createUser("statemgr", RoleName.STATE_MANAGER, bihar, null, null, null);

        // hierarchy pass: state manager can create district manager in own scope
        mockMvc.perform(post("/api/users")
                        .with(user("statemgr").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "dm1",
                                "password", "Pass@1234",
                                "email", "dm1@example.com",
                                "phone", "9999999991",
                                "role", "DISTRICT_MANAGER",
                                "assignedStateId", bihar.getId(),
                                "assignedDistrictId", patna.getId()
                        ))))
                .andExpect(status().isCreated());

        // hierarchy fail: state manager cannot skip to block manager
        mockMvc.perform(post("/api/users")
                        .with(user("statemgr").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "bm1",
                                "password", "Pass@1234",
                                "email", "bm1@example.com",
                                "phone", "9999999992",
                                "role", "BLOCK_MANAGER",
                                "assignedStateId", bihar.getId(),
                                "assignedDistrictId", patna.getId(),
                                "assignedBlockId", patnaBlock.getId()
                        ))))
                .andExpect(status().isForbidden());

        User inScopeDistrictManager = createUser("dm-scope", RoleName.DISTRICT_MANAGER, bihar, patna, patnaBlock, centerA);
        User outScopeDistrictManager = createUser("dm-out", RoleName.DISTRICT_MANAGER, jharkhand, ranchi, ranchiBlock, centerB);
        setToggle(stateMgr, RoleName.DISTRICT_MANAGER, true, true, false);

        // scope pass
        mockMvc.perform(patch("/api/users/{id}/safe", inScopeDistrictManager.getId())
                        .with(user("statemgr").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "updated.in.scope@example.com"))))
                .andExpect(status().isOk());

        // scope fail for direct API call
        mockMvc.perform(patch("/api/users/{id}/safe", outScopeDistrictManager.getId())
                        .with(user("statemgr").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "updated.out.scope@example.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleRulesAndTogglePanelAccessAreEnforced() throws Exception {
        User superAdmin = createUser("supera", RoleName.SUPER_ADMIN, null, null, null, null);
        User admin = createUser("admina", RoleName.ADMIN, null, null, null, null);
        User stateMgr = createUser("statea", RoleName.STATE_MANAGER, bihar, null, null, null);
        User target = createUser("dm-toggle", RoleName.DISTRICT_MANAGER, bihar, patna, patnaBlock, centerA);

        setToggle(stateMgr, RoleName.DISTRICT_MANAGER, true, false, false);

        mockMvc.perform(patch("/api/users/{id}/safe", target.getId())
                        .with(user("statea").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "7000000000"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/users/{id}/toggles", stateMgr.getId())
                        .with(user("supera").roles("SUPER_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "targetRole", "DISTRICT_MANAGER",
                                "canEdit", true
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/users/{id}/safe", target.getId())
                        .with(user("statea").roles("STATE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "7111111111"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}/toggles", stateMgr.getId())
                        .with(user("supera").roles("SUPER_ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}/toggles", stateMgr.getId())
                        .with(user("admina").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/{id}/toggles", stateMgr.getId())
                        .with(user("statea").roles("STATE_MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void dropdownAndDangerousFieldRulesAreEnforced() throws Exception {
        createUser("state-drop", RoleName.STATE_MANAGER, bihar, null, null, null);
        User blockManager = createUser("block-drop", RoleName.BLOCK_MANAGER, bihar, patna, patnaBlock, centerA);
        User doctor = createUser("doctor1", RoleName.DOCTOR, bihar, patna, patnaBlock, centerA);
        setToggle(blockManager, RoleName.DOCTOR, true, true, true);

        mockMvc.perform(get("/api/roles/assignable")
                        .with(user("state-drop").roles("STATE_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("district_manager"))
                .andExpect(jsonPath("$.data.roles.length()").value(1));

        mockMvc.perform(get("/api/roles/assignable")
                        .with(user("block-drop").roles("BLOCK_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles").value(org.hamcrest.Matchers.hasItem("doctor")));

        mockMvc.perform(get("/api/users/{id}/editable", doctor.getId())
                        .with(user("block-drop").roles("BLOCK_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").doesNotExist())
                .andExpect(jsonPath("$.data.assignedStateId").doesNotExist());

        mockMvc.perform(patch("/api/users/{id}/safe", doctor.getId())
                        .with(user("block-drop").roles("BLOCK_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivationAndPrivilegeEscalationRulesAreEnforced() throws Exception {
        User superAdmin1 = createUser("sa1", RoleName.SUPER_ADMIN, null, null, null, null);
        User superAdmin2 = createUser("sa2", RoleName.SUPER_ADMIN, null, null, null, null);
        User admin = createUser("admin-deact", RoleName.ADMIN, null, null, null, null);
        User associate = createUser("assoc", RoleName.ASSOCIATE, bihar, patna, patnaBlock, centerA);

        mockMvc.perform(post("/api/users/{id}/deactivate", superAdmin1.getId())
                        .with(user("admin-deact").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users/{id}/deactivate", superAdmin1.getId())
                        .with(user("sa2").roles("SUPER_ADMIN")))
                .andExpect(status().isOk());

        User deactivated = userRepository.findById(superAdmin1.getId()).orElseThrow();
        assertEquals(UserStatus.DEACTIVATED, deactivated.getStatus());

        User doctor = createUser("doctor-priv", RoleName.DOCTOR, bihar, patna, patnaBlock, centerA);
        User blockMgr = createUser("block-priv", RoleName.BLOCK_MANAGER, bihar, patna, patnaBlock, centerA);
        setToggle(blockMgr, RoleName.DOCTOR, true, true, true);

        mockMvc.perform(patch("/api/users/{id}/safe", doctor.getId())
                        .with(user("block-priv").roles("BLOCK_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("assignedCenterId", centerB.getId()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users/{id}/deactivate", associate.getId())
                        .with(user("sa2").roles("SUPER_ADMIN")))
                .andExpect(status().isOk());

        assertThrows(Exception.class, () -> authService.login(new com.apanaswastha.erp.dto.request.auth.LoginRequest() {{
            setUsername("assoc");
            setPassword("Pass@1234");
        }}));

        User stillPresent = userRepository.findById(associate.getId()).orElseThrow();
        assertFalse(stillPresent.isDeleted());
    }

    @Test
    void dangerousFieldAdminAndSuperAdminPatchAllowed() throws Exception {
        User admin = createUser("admin-df", RoleName.ADMIN, null, null, null, null);
        User superAdmin = createUser("sa-df", RoleName.SUPER_ADMIN, null, null, null, null);
        User doctor = createUser("doctor-df", RoleName.DOCTOR, bihar, patna, patnaBlock, centerA);

        mockMvc.perform(patch("/api/users/{id}", doctor.getId())
                        .with(user("admin-df").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "PHARMACIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("PHARMACIST"));

        mockMvc.perform(patch("/api/users/{id}", doctor.getId())
                        .with(user("sa-df").roles("SUPER_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "DOCTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));
    }

    private User createUser(String username, RoleName roleName, State state, District district, Block block, Center center) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("Pass@1234"));
        user.setEmail(username + "@example.com");
        user.setPhone("900000" + Math.abs(username.hashCode() % 10000));
        user.setRole(role);
        user.setAssignedState(state);
        user.setAssignedDistrict(district);
        user.setAssignedBlock(block);
        user.setAssignedCenter(center);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private void setToggle(User user, RoleName targetRole, boolean canCreate, boolean canEdit, boolean canDelete) {
        UserPermissionToggle toggle = toggleRepository.findByUserIdAndTargetRole(user.getId(), targetRole)
                .orElseGet(() -> {
                    UserPermissionToggle created = new UserPermissionToggle();
                    created.setUser(user);
                    created.setTargetRole(targetRole);
                    return created;
                });
        toggle.setCanCreate(canCreate);
        toggle.setCanEdit(canEdit);
        toggle.setCanDelete(canDelete);
        toggleRepository.save(toggle);
    }
}
