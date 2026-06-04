package com.zzw.chatserver.controller;

import com.zzw.chatserver.pojo.vo.ModifyFriendBeiZhuRequestVo;
import com.zzw.chatserver.pojo.vo.ModifyFriendFenZuRequestVo;
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
 * GoodFriendController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GoodFriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private String userBToken;

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
     * TC_FRIEND_001: 发送好友请求
     */
    @Test
    public void testSendFriendRequest() throws Exception {
        mockMvc.perform(post("/api/friend/add")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"message\": \"我是用户A\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_002: 接受好友请求
     */
    @Test
    public void testAcceptFriendRequest() throws Exception {
        // 先发送好友请求
        testSendFriendRequest();

        // 接受请求
        mockMvc.perform(post("/api/friend/accept")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"validateId\": \"1\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_003: 拒绝好友请求
     */
    @Test
    public void testRejectFriendRequest() throws Exception {
        // 先发送好友请求
        testSendFriendRequest();

        // 拒绝请求
        mockMvc.perform(post("/api/friend/reject")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"validateId\": \"1\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_004: 删除好友
     */
    @Test
    public void testDeleteFriend() throws Exception {
        // 先建立好友关系
        testAcceptFriendRequest();

        // 删除好友
        mockMvc.perform(post("/api/friend/delete")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"friendId\": \"userb\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_006: 自己向自己发送好友请求
     */
    @Test
    public void testSendFriendRequestToSelf() throws Exception {
        mockMvc.perform(post("/api/friend/add")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"usera\", \"message\": \"自己\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("自己")));
    }

    /**
     * TC_FRIEND_007: 查看好友列表
     */
    @Test
    public void testGetFriendList() throws Exception {
        // 先建立好友关系
        testAcceptFriendRequest();

        // 查询好友列表
        mockMvc.perform(get("/api/friend/list")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    /**
     * TC_FRIEND_NOTE_001: 设置好友备注
     */
    @Test
    public void testSetFriendRemark() throws Exception {
        // 先建立好友关系
        testAcceptFriendRequest();

        ModifyFriendBeiZhuRequestVo vo = new ModifyFriendBeiZhuRequestVo();
        vo.setFriendId("userb");
        vo.setFriendBeiZhuName("老同学");

        mockMvc.perform(post("/api/friend/remark")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_GROUP_001: 创建分组
     */
    @Test
    public void testCreateFriendGroup() throws Exception {
        mockMvc.perform(post("/api/friend/group/create")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"家人\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_GROUP_002: 修改分组名称
     */
    @Test
    public void testUpdateFriendGroup() throws Exception {
        // 先创建分组
        testCreateFriendGroup();

        mockMvc.perform(put("/api/friend/group/update")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\": \"1\", \"groupName\": \"亲戚\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_GROUP_003: 删除分组
     */
    @Test
    public void testDeleteFriendGroup() throws Exception {
        // 先创建分组
        testCreateFriendGroup();

        mockMvc.perform(delete("/api/friend/group/1")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_FRIEND_GROUP_005: 移动好友到分组
     */
    @Test
    public void testMoveFriendToGroup() throws Exception {
        // 先建立好友关系和创建分组
        testAcceptFriendRequest();
        testCreateFriendGroup();

        ModifyFriendFenZuRequestVo vo = new ModifyFriendFenZuRequestVo();
        vo.setFriendId("userb");
        vo.setNewFenZuName("家人");

        mockMvc.perform(post("/api/friend/moveToGroup")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
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

}
