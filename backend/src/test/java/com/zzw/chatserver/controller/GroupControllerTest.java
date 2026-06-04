package com.zzw.chatserver.controller;

import com.zzw.chatserver.pojo.vo.CreateGroupRequestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * GroupController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private String userBToken;
    private String groupId;

    @BeforeEach
    public void setUp() throws Exception {
        // 注册两个测试用户
        registerUser("usera", "password123");
        registerUser("userb", "password123");

        // 登录获取token
        userAToken = loginUser("usera", "password123");
        userBToken = loginUser("userb", "password123");
    }

    /**
     * TC_GROUP_001: 创建群组
     */
    @Test
    public void testCreateGroup() throws Exception {
        CreateGroupRequestVo vo = new CreateGroupRequestVo();
        vo.setTitle("测试群组");
        vo.setDesc("这是一个测试群组");
        vo.setHolderName("usera");
        vo.setHolderUserId("usera");

        mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data.title", equalTo("测试群组")));
    }

    /**
     * TC_GROUP_002: 创建群组-空成员列表
     */
    @Test
    public void testCreateGroupEmptyMembers() throws Exception {
        CreateGroupRequestVo vo = new CreateGroupRequestVo();
        vo.setTitle("空群组");
        vo.setDesc("无成员");
        vo.setHolderName("usera");
        vo.setHolderUserId("usera");

        mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("成员")));
    }

    /**
     * TC_GROUP_003: 修改群组信息
     */
    @Test
    public void testUpdateGroupInfo() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 修改群组信息
        mockMvc.perform(put("/api/group/" + groupId)
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"修改后的群组\", \"groupNotice\": \"新公告\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data.groupName", equalTo("修改后的群组")));
    }

    /**
     * TC_GROUP_004: 非群主修改群组信息
     */
    @Test
    public void testUpdateGroupInfoNotOwner() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 非群主修改
        mockMvc.perform(put("/api/group/" + groupId)
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"修改后的群组\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("权限")));
    }

    /**
     * TC_GROUP_005: 解散群组
     */
    @Test
    public void testDisbandGroup() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 解散群组
        mockMvc.perform(delete("/api/group/" + groupId)
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_GROUP_006: 非群主解散群组
     */
    @Test
    public void testDisbandGroupNotOwner() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 非群主解散
        mockMvc.perform(delete("/api/group/" + groupId)
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("权限")));
    }

    /**
     * TC_GROUP_007: 查询群组信息
     */
    @Test
    public void testGetGroupInfo() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 查询群组信息
        mockMvc.perform(get("/api/group/" + groupId)
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data.groupName", notNullValue()));
    }

    /**
     * TC_GROUP_008: 搜索群组
     */
    @Test
    public void testSearchGroup() throws Exception {
        mockMvc.perform(get("/api/group/search?keyword=测试")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    /**
     * TC_GROUP_MEMBER_001: 邀请好友加入群组
     */
    @Test
    public void testInviteFriendToGroup() throws Exception {
        // 先建立好友关系和创建群组
        establishFriendship();
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 邀请好友加入
        mockMvc.perform(post("/api/group/" + groupId + "/invite")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"userb\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_GROUP_MEMBER_006: 退出群组
     */
    @Test
    public void testQuitGroup() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 用户B退出群组
        mockMvc.perform(post("/api/group/" + groupId + "/quit")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_GROUP_MEMBER_008: 删除群成员
     */
    @Test
    public void testRemoveGroupMember() throws Exception {
        // 先创建群组
        String response = mockMvc.perform(post("/api/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"测试\", \"groupNotice\": \"测试\", \"memberIds\": [\"usera\", \"userb\"]}")
        ).andReturn().getResponse().getContentAsString();

        groupId = objectMapper.readTree(response).get("data").get("id").asText();

        // 删除成员
        mockMvc.perform(post("/api/group/" + groupId + "/removeMember")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"userb\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    // 辅助方法

    private void registerUser(String username, String password) throws Exception {
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"rePassword\": \"" + password + "\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    private String loginUser(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    private void establishFriendship() throws Exception {
        // 发送好友请求
        mockMvc.perform(post("/api/friend/add")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"message\": \"加我好友\"}"))
                .andExpect(status().is2xxSuccessful());

        // 接受好友请求
        mockMvc.perform(post("/api/friend/accept")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"validateId\": \"1\"}"))
                .andExpect(status().is2xxSuccessful());
    }

}
