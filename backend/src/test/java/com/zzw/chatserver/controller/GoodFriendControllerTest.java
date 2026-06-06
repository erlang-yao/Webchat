package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.ResultEnum;
import com.zzw.chatserver.pojo.vo.MyFriendListResultVo;
import com.zzw.chatserver.service.GoodFriendService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GoodFriendControllerTest {

    @Mock
    private GoodFriendService goodFriendService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GoodFriendController controller = new GoodFriendController();
        ReflectionTestUtils.setField(controller, "goodFriendService", goodFriendService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-a", null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyFriendsListReturnsCurrentResponseShape() throws Exception {
        MyFriendListResultVo friend = new MyFriendListResultVo();
        friend.setId("user-b");
        friend.setNickname("friend");
        when(goodFriendService.getMyFriendsList("user-a")).thenReturn(Collections.singletonList(friend));

        mockMvc.perform(get("/goodFriend/getMyFriendsList").param("userId", "user-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())))
                .andExpect(jsonPath("$.data.myFriendsList[0].id", equalTo("user-b")));
    }

    @Test
    void recentConversationDelegatesToService() throws Exception {
        when(goodFriendService.getRecentConversation(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/goodFriend/recentConversationList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-a\",\"recentFriendIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.singleRecentConversationList").isArray());
    }

    @Test
    void deleteFriendAllowsCurrentUser() throws Exception {
        mockMvc.perform(delete("/goodFriend/deleteGoodFriend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userM\":\"user-a\",\"userY\":\"user-b\",\"roomId\":\"room-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())));

        verify(goodFriendService).deleteFriend(any());
    }

    @Test
    void deleteFriendRejectsDifferentUser() throws Exception {
        mockMvc.perform(delete("/goodFriend/deleteGoodFriend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userM\":\"user-b\",\"userY\":\"user-a\",\"roomId\":\"room-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.ILLEGAL_OPERATION.getCode())));

        verify(goodFriendService, never()).deleteFriend(any());
    }
}
