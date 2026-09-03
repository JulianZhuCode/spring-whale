package io.github.springwhale.platform.rbac.controller;

import tools.jackson.databind.ObjectMapper;
import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dto.request.GroupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@DisplayName("GroupController 集成测试")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupRepository groupRepository;

    private GroupEntity rootGroup;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();

        rootGroup = new GroupEntity();
        rootGroup.setCode("ROOT");
        rootGroup.setName("Root Dept");
        rootGroup.setPath("/");
        rootGroup.setStatus(1);
        rootGroup = groupRepository.save(rootGroup);

        GroupEntity childGroup = new GroupEntity();
        childGroup.setCode("CHILD");
        childGroup.setName("Child Dept");
        childGroup.setParentId(rootGroup.getId());
        childGroup.setPath("/" + rootGroup.getId() + "/");
        childGroup.setStatus(1);
        groupRepository.save(childGroup);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("获取部门树")
    void tree() throws Exception {
        mockMvc.perform(get("/api/rbac/groups/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Root Dept"))
                .andExpect(jsonPath("$.data[0].children").isArray());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("分页查询部门列表")
    void findAll() throws Exception {
        mockMvc.perform(get("/api/rbac/groups")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("根据关键字搜索部门")
    void findAllWithKeyword() throws Exception {
        mockMvc.perform(get("/api/rbac/groups")
                        .param("page", "0")
                        .param("size", "20")
                        .param("keyword", "Child"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Child Dept"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("根据ID查询部门")
    void findById() throws Exception {
        mockMvc.perform(get("/api/rbac/groups/{id}", rootGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("ROOT"))
                .andExpect(jsonPath("$.data.name").value("Root Dept"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("查询不存在的部门 - 返回业务错误")
    void findByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/rbac/groups/{id}", 9999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group:create"})
    @DisplayName("创建根部门")
    void createRoot() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setCode("NEW_ROOT");
        request.setName("New Root");
        request.setStatus(1);

        mockMvc.perform(post("/api/rbac/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("NEW_ROOT"))
                .andExpect(jsonPath("$.data.name").value("New Root"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group:create"})
    @DisplayName("创建子部门")
    void createChild() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setCode("NEW_CHILD");
        request.setName("New Child");
        request.setParentId(rootGroup.getId());
        request.setStatus(1);

        mockMvc.perform(post("/api/rbac/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("NEW_CHILD"))
                .andExpect(jsonPath("$.data.parentId").value(rootGroup.getId()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group:update"})
    @DisplayName("更新部门")
    void update() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setCode(rootGroup.getCode());
        request.setName("Updated Root");
        request.setStatus(1);

        mockMvc.perform(put("/api/rbac/groups/{id}", rootGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Root"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group:delete"})
    @DisplayName("删除部门")
    void deleteGroup() throws Exception {
        mockMvc.perform(delete("/api/rbac/groups/{id}", rootGroup.getId()))
                .andExpect(status().isOk());
    }
}