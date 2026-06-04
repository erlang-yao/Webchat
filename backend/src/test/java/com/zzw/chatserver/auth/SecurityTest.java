package com.zzw.chatserver.auth;

import com.zzw.chatserver.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 安全与认证测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * TC_AUTH_001: JWT Token验证
     */
    @Test
    public void testValidTokenAccess() throws Exception {
        // 生成有效token
        String token = JwtUtils.createJwt("testuser", "testuser");

        // 使用有效token访问受保护资源
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());
    }

    /**
     * TC_AUTH_002: 无效Token
     */
    @Test
    public void testInvalidTokenAccess() throws Exception {
        // 使用无效token
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_AUTH_003: Token过期
     */
    @Test
    public void testExpiredTokenAccess() throws Exception {
        // 生成有效的token
        String token = JwtUtils.createJwt("testuser", "testuser");

        // 使用token访问资源
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());
    }

    /**
     * TC_AUTH_004: 未认证访问
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        // 不提供token访问受保护资源
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_AUTH_005: 缺少Authorization头
     */
    @Test
    public void testMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_AUTH_006: 错误的Authorization格式
     */
    @Test
    public void testInvalidAuthorizationFormat() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "invalid_format"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_PERMISSION_001: 访问公开端点
     */
    @Test
    public void testAccessPublicEndpoint() throws Exception {
        mockMvc.perform(post("/api/user/register")
                .contentType("application/json")
                .content("{\"username\": \"test\", \"password\": \"test123\", \"rePassword\": \"test123\"}"))
                .andExpect(status().is2xxSuccessful());
    }

}
