package com.zzw.chatserver.controller;

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
 * SingleMessageController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SingleMessageControllerTest {

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

        // 建立好友关系
        userAToken = loginUser("usera", "password123");
        userBToken = loginUser("userb", "password123");
        establishFriendship();
    }

    /**
     * TC_MSG_SINGLE_001: 发送文本消息（通过REST接口）
     */
    @Test
    public void testSendTextMessage() throws Exception {
        mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"你好\", \"type\": \"text\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_MSG_SINGLE_002: 发送消息给非好友
     */
    @Test
    public void testSendMessageToNonFriend() throws Exception {
        // 注册第三个用户（不是好友）
        registerUser("userc", "password123");

        mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userc\", \"content\": \"你好\", \"type\": \"text\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("好友")));
    }

    /**
     * TC_MSG_SINGLE_004: 撤回消息
     */
    @Test
    public void testRecallMessage() throws Exception {
        // 先发送消息
        String response = mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"你好\", \"type\": \"text\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String messageId = objectMapper.readTree(response).get("data").get("id").asText();

        // 撤回消息
        mockMvc.perform(post("/api/message/recall")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"messageId\": \"" + messageId + "\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_MSG_SINGLE_006: 查询聊天历史
     */
    @Test
    public void testGetChatHistory() throws Exception {
        // 先发送几条消息
        mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"消息1\", \"type\": \"text\"}"));

        mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"消息2\", \"type\": \"text\"}"));

        // 查询聊天历史
        mockMvc.perform(get("/api/message/history?targetUserId=userb&page=1&pageSize=10")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    /**
     * TC_MSG_SINGLE_007: 标记消息已读
     */
    @Test
    public void testMarkMessageAsRead() throws Exception {
        // 先发送消息
        String response = mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"你好\", \"type\": \"text\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String messageId = objectMapper.readTree(response).get("data").get("id").asText();

        // 标记已读
        mockMvc.perform(post("/api/message/markAsRead")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"messageId\": \"" + messageId + "\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)));
    }

    /**
     * TC_MSG_SINGLE_008: 删除消息
     */
    @Test
    public void testDeleteMessage() throws Exception {
        // 先发送消息
        String response = mockMvc.perform(post("/api/message/sendSingle")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"userb\", \"content\": \"你好\", \"type\": \"text\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String messageId = objectMapper.readTree(response).get("data").get("id").asText();

        // 删除消息
        mockMvc.perform(delete("/api/message/" + messageId)
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON))
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
