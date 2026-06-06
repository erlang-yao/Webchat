package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.ResultEnum;
import com.zzw.chatserver.pojo.User;
import com.zzw.chatserver.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void registerReturnsCurrentSuccessContract() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("code", ResultEnum.REGISTER_SUCCESS.getCode());
        result.put("msg", ResultEnum.REGISTER_SUCCESS.getMessage());
        result.put("userCode", "10000001");
        when(userService.register(any())).thenReturn(result);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"123456\",\"rePassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.REGISTER_SUCCESS.getCode())))
                .andExpect(jsonPath("$.data.userCode", equalTo("10000001")));
    }

    @Test
    void registerReturnsBusinessErrorInResponseEnvelope() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("code", ResultEnum.USER_HAS_EXIST.getCode());
        result.put("msg", ResultEnum.USER_HAS_EXIST.getMessage());
        when(userService.register(any())).thenReturn(result);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"123456\",\"rePassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.USER_HAS_EXIST.getCode())));
    }

    @Test
    void getUserInfoReturnsServiceResult() throws Exception {
        ObjectId id = new ObjectId();
        User user = new User();
        user.setUserId(id);
        user.setUsername("testuser");
        when(userService.getUserInfo(id.toHexString())).thenReturn(user);

        mockMvc.perform(get("/user/getUserInfo").param("uid", id.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())))
                .andExpect(jsonPath("$.data.userInfo.username", equalTo("testuser")));
    }

    @Test
    void updateUserInfoReturnsSuccessWhenServiceHasNoError() throws Exception {
        when(userService.updateUserInfo(any())).thenReturn(Collections.emptyMap());

        mockMvc.perform(post("/user/updateUserInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"507f1f77bcf86cd799439011\",\"field\":\"nickname\",\"value\":\"new-name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())));

        verify(userService).updateUserInfo(any());
    }
}
