package com.zzw.chatserver.controller;

import com.zzw.chatserver.pojo.vo.LoginRequestVo;
import com.zzw.chatserver.pojo.vo.RegisterRequestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * UserController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequestVo registerRequestVo;
    private LoginRequestVo loginRequestVo;

    @BeforeEach
    public void setUp() {
        registerRequestVo = new RegisterRequestVo();
        registerRequestVo.setUsername("testuser");
        registerRequestVo.setPassword("password123");
        registerRequestVo.setRePassword("password123");

        loginRequestVo = new LoginRequestVo();
        loginRequestVo.setUsername("testuser");
        loginRequestVo.setPassword("password123");
    }

    /**
     * TC_USER_001: 正常注册用户
     */
    @Test
    public void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data.username", equalTo("testuser")))
                .andExpect(jsonPath("$.data.email", equalTo("test@example.com")));
    }

    /**
     * TC_USER_002: 用户名已存在
     */
    @Test
    public void testRegisterUserExists() throws Exception {
        // 先注册一个用户
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().is2xxSuccessful());

        // 重复注册相同用户名
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("用户已存在")));
    }

    /**
     * TC_USER_003: 密码为空
     */
    @Test
    public void testRegisterEmptyPassword() throws Exception {
        registerRequestVo.setPassword(null);

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("密码")));
    }

    /**
     * TC_USER_004: 重复密码不匹配
     */
    @Test
    public void testRegisterPasswordMismatch() throws Exception {
        registerRequestVo.setRePassword("differentpassword");

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("密码")));
    }

    /**
     * TC_LOGIN_001: 正常登录
     */
    @Test
    public void testLoginSuccess() throws Exception {
        // 先注册用户
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().is2xxSuccessful());

        // 登录
        MvcResult result = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestVo)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println("Login response: " + response);
    }

    /**
     * TC_LOGIN_002: 用户不存在
     */
    @Test
    public void testLoginUserNotFound() throws Exception {
        loginRequestVo.setUsername("nonexistent");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestVo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("用户")));
    }

    /**
     * TC_LOGIN_003: 密码错误
     */
    @Test
    public void testLoginWrongPassword() throws Exception {
        // 先注册用户
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().is2xxSuccessful());

        // 错误密码登录
        loginRequestVo.setPassword("wrongpassword");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestVo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("密码")));
    }

    /**
     * TC_AUTH_004: 未认证访问
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_USER_UPDATE_001: 修改用户昵称 (需要认证)
     */
    @Test
    public void testUpdateUserNickname() throws Exception {
        // 先注册和登录
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestVo)))
                .andExpect(status().is2xxSuccessful());

        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestVo)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // 提取token并修改昵称
        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("data").get("token").asText();

        mockMvc.perform(put("/api/user/nickname")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\": \"新昵称\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.nickname", equalTo("新昵称")));
    }

}
